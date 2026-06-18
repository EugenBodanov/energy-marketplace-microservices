# Google Cloud bootstrap

This Terraform root prepares Google Cloud for the Energy Marketplace microservices.

It creates:

- required Google APIs
- Artifact Registry Docker repository
- GitHub deploy service account
- Workload Identity Federation provider for GitHub Actions
- runtime service accounts for each Cloud Run service

The GitHub workflow `.github/workflows/bootstrap-gcp.yml` runs this root manually.

## First run authentication

The first run needs one of these auth paths:

- `GCP_BOOTSTRAP_CREDENTIALS_JSON`: temporary service account JSON with permissions to create IAM, APIs, Artifact Registry, and a GCS state bucket.
- or existing WIF secrets: `GCP_WORKLOAD_IDENTITY_PROVIDER` and `GCP_DEPLOY_SERVICE_ACCOUNT`.

After a successful apply, copy these outputs into GitHub secrets:

- `workload_identity_provider` -> `GCP_WORKLOAD_IDENTITY_PROVIDER`
- `deploy_service_account_email` -> `GCP_DEPLOY_SERVICE_ACCOUNT`

Postgres, RabbitMQ, runtime Secret Manager secrets, Cloud Run services, and service-to-service IAM are owned by `infra/terraform/envs/dev`.
