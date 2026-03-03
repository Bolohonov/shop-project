#!/bin/bash
set -e

APP=${1:-shop-app}
TAG=${2:-latest}

VPS="root@77.222.35.2"
SERVER="michael@10.0.0.2"
REMOTE_BUILD_DIR="/home/michael/build/$APP"
REMOTE_SECRETS_DIR="/home/michael/secrets/$APP"
HELM_CHART="./shop-helm"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log()  { echo -e "${GREEN}[deploy]${NC} $1"; }
warn() { echo -e "${YELLOW}[warn]${NC} $1"; }
fail() { echo -e "${RED}[error]${NC} $1"; exit 1; }

log "Starting deploy of $APP:$TAG"

[ ! -d "$HELM_CHART" ] && fail "Helm chart not found: $HELM_CHART"

log "Syncing sources to server..."
ssh -J $VPS $SERVER "mkdir -p $REMOTE_BUILD_DIR"

for SERVICE in shop-backend shop-frontend; do
  if [ -d "./$SERVICE" ]; then
    log "  Syncing $SERVICE..."
    rsync -az --delete \
      --exclude='target/' \
      --exclude='node_modules/' \
      --exclude='.git/' \
      --exclude='*.class' \
      -e "ssh -J $VPS" \
      ./$SERVICE/ $SERVER:$REMOTE_BUILD_DIR/$SERVICE/
  fi
done

log "Building images on server..."
ssh -J $VPS $SERVER bash << REMOTEBUILD
set -e
REGISTRY="localhost:5000"
TAG="$TAG"
BUILD_DIR="$REMOTE_BUILD_DIR"

echo "[server] Building shop-backend..."
docker build -t \$REGISTRY/shop-backend:\$TAG \$BUILD_DIR/shop-backend/

echo "[server] Building shop-frontend..."
docker build -t \$REGISTRY/shop-frontend:\$TAG \$BUILD_DIR/shop-frontend/

echo "[server] Pushing to local registry..."
docker push \$REGISTRY/shop-backend:\$TAG
docker push \$REGISTRY/shop-frontend:\$TAG
echo "[server] Done building"
REMOTEBUILD

log "Syncing helm chart..."
ssh -J $VPS $SERVER "mkdir -p $REMOTE_BUILD_DIR/helm"
rsync -az --delete \
  --exclude='values.prod.yaml' \
  -e "ssh -J $VPS" \
  $HELM_CHART/ $SERVER:$REMOTE_BUILD_DIR/helm/

log "Deploying to k3s..."
ssh -J $VPS $SERVER bash << REMOTEDEPLOY
set -e
SECRETS="$REMOTE_SECRETS_DIR/values.prod.yaml"
CHART="$REMOTE_BUILD_DIR/helm"

[ ! -f "\$SECRETS" ] && echo "ERROR: values.prod.yaml not found at \$SECRETS" && exit 1

echo "[server] Clearing k3s image cache..."
sudo crictl rmi localhost:5000/shop-backend:$TAG 2>/dev/null || true
sudo crictl rmi localhost:5000/shop-frontend:$TAG 2>/dev/null || true

helm upgrade --install $APP \$CHART \
  --namespace shop --create-namespace \
  --values \$SECRETS \
  --set backend.image.tag=$TAG \
  --set frontend.image.tag=$TAG

echo "[server] Restarting deployments..."
kubectl -n shop rollout restart deployment/$APP-backend
kubectl -n shop rollout restart deployment/$APP-frontend

echo "[server] Checking rollout..."
kubectl -n shop rollout status deployment/$APP-backend --timeout=120s
kubectl -n shop rollout status deployment/$APP-frontend --timeout=120s
echo "[server] Deploy complete"
REMOTEDEPLOY

log "Deploy of $APP:$TAG complete"
