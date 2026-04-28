# Kubernetes (EKS)

This repo deploys two services:

- `onboarding-service` (port 8081)
- `mcp-server` (port 9092)

Both are deployed as `ClusterIP` services (private) by default.

## Apply locally (with kubectl access)

Dev overlay:

```sh
kubectl apply -k k8s/overlays/dev
```

## CI/CD (GitHub Actions)

Workflow: `.github/workflows/eks-deploy.yml`

Required GitHub secrets:

- `AWS_ROLE_ARN`: IAM role to assume via OIDC
- `EKS_CLUSTER_NAME`: EKS cluster name
- `ECR_REPO_ONBOARDING`: ECR repository name (not URI), e.g. `onboarding-service`
- `ECR_REPO_MCP`: ECR repository name (not URI), e.g. `mcp-server`

Defaults:

- AWS region: `eu-north-1`
- Namespace: `onboarding`
- Overlay: `k8s/overlays/dev`

## Notes

- Update `k8s/base/secret.yaml` values before deploying to real environments.
- Do not commit real secrets; use an external secret manager in production.

