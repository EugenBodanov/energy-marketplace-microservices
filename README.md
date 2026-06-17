# Energy Marketplace

Microservices-based energy marketplace with automated Google Cloud deployment through GitHub Actions and Terraform.

## Deployment Setup

The deployment is split into two Terraform roots:

- `infra/terraform/bootstrap` prepares the Google Cloud project foundation.
- `infra/terraform/envs/dev` creates the dev runtime resources and deploys the microservices.

GitHub Actions builds Docker images, pushes them to Artifact Registry, writes the image URIs into Terraform variables, and runs `terraform apply` for the dev environment.

### What Must Be Configured Outside GitHub Secrets

Before the automated deployment can run, configure these items:

- A Google Cloud project with billing enabled.
- A GitHub repository connected to this codebase.
- GitHub Actions enabled for the repository.
- One initial bootstrap authentication path:
  - preferred first run: temporary `GCP_BOOTSTRAP_CREDENTIALS_JSON` secret, or
  - existing Workload Identity Federation secrets if they were already created.
- Run the `Bootstrap Google Cloud infrastructure` workflow once with `mode = apply`.
- After bootstrap, copy Terraform outputs into GitHub secrets:
  - `workload_identity_provider` -> `GCP_WORKLOAD_IDENTITY_PROVIDER`
  - `deploy_service_account_email` -> `GCP_DEPLOY_SERVICE_ACCOUNT`
- Keep `GAR_LOCATION`, `GAR_REPOSITORY`, `GCP_REGION`, and `GCP_ZONE` aligned with the values used by Terraform.
- If RabbitMQ VM was stopped with the manual workflow, start it before deploying or using services that connect to RabbitMQ.

Bootstrap creates the Terraform state bucket, enables required Google APIs, creates Artifact Registry, configures GitHub Workload Identity Federation, creates the deploy service account, and creates runtime service accounts.

The dev Terraform root creates Cloud SQL, RabbitMQ VM, runtime secrets, Cloud Run services, and Cloud Run IAM.

### GitHub Secrets

Configure these secrets in GitHub repository settings under `Settings -> Secrets and variables -> Actions`.

| Secret | Required | Used by | Description |
| --- | --- | --- | --- |
| `GCP_PROJECT_ID` | Yes | Bootstrap, deploy, RabbitMQ VM workflows | Google Cloud project ID. |
| `GCP_REGION` | Yes | Deploy workflow | Google Cloud region for Cloud Run and Terraform dev resources, for example `europe-west1`. |
| `GCP_ZONE` | Optional | Deploy workflow | Zone for the RabbitMQ VM. If empty, deploy uses `${GCP_REGION}-b`. Manual VM workflows also have a zone input with default `europe-west1-b`. |
| `GAR_LOCATION` | Yes | Deploy workflow | Artifact Registry location, for example `europe-west1`. Must match bootstrap input. |
| `GAR_REPOSITORY` | Yes | Deploy workflow | Artifact Registry Docker repository ID, for example `energy-marketplace`. Must match bootstrap input. |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | Yes after bootstrap | Bootstrap, deploy, RabbitMQ VM workflows | Workload Identity Provider resource name from bootstrap output `workload_identity_provider`. |
| `GCP_DEPLOY_SERVICE_ACCOUNT` | Yes after bootstrap | Bootstrap, deploy, RabbitMQ VM workflows | Deploy service account email from bootstrap output `deploy_service_account_email`. |
| `GCP_BOOTSTRAP_CREDENTIALS_JSON` | First bootstrap only | Bootstrap workflow | Temporary service account JSON used only to create the first WIF/deployer setup. Remove it after WIF secrets are configured. |

### Workflow Order

1. Add `GCP_PROJECT_ID` and temporary `GCP_BOOTSTRAP_CREDENTIALS_JSON`.
2. Run `Bootstrap Google Cloud infrastructure` manually with `mode = apply`.
3. Copy bootstrap outputs into `GCP_WORKLOAD_IDENTITY_PROVIDER` and `GCP_DEPLOY_SERVICE_ACCOUNT`.
4. Add deployment secrets: `GCP_REGION`, optional `GCP_ZONE`, `GAR_LOCATION`, and `GAR_REPOSITORY`.
5. Remove `GCP_BOOTSTRAP_CREDENTIALS_JSON` if it is no longer needed.
6. Run `Build images and deploy dev with Terraform` manually, or push to `main`.

### Local Terraform Init

The Terraform backend uses Google Cloud Storage. If you run Terraform locally after bootstrap, initialize the dev root with the same backend config used in CI:

```powershell
$env:PROJECT_ID = "your-gcp-project-id"

terraform -chdir="infra/terraform/envs/dev" init `
  -backend-config="bucket=$($env:PROJECT_ID)-energy-marketplace-tfstate" `
  -backend-config="prefix=infra/terraform/envs/dev"
```

For bootstrap:

Local bootstrap needs Google Cloud credentials on your machine with enough permissions to create and manage the bootstrap resources. A common setup is:

```powershell
gcloud auth application-default login
gcloud config set project "your-gcp-project-id"
```

The account must be able to enable APIs, create the Terraform state bucket, create IAM service accounts and IAM bindings, create Workload Identity Federation resources, and create Artifact Registry repositories.

The backend bucket must already exist before `terraform init`. The GitHub bootstrap workflow creates it automatically before running Terraform. Locally, create it once before initializing:

```powershell
$env:PROJECT_ID = "your-gcp-project-id"
$env:TF_STATE_BUCKET = "$($env:PROJECT_ID)-energy-marketplace-tfstate"

gcloud services enable serviceusage.googleapis.com cloudresourcemanager.googleapis.com storage.googleapis.com --project $env:PROJECT_ID

gcloud storage buckets create "gs://$env:TF_STATE_BUCKET" `
  --project $env:PROJECT_ID `
  --location EU `
  --uniform-bucket-level-access

gcloud storage buckets update "gs://$env:TF_STATE_BUCKET" --versioning
```

Then initialize Terraform:

```powershell
$env:PROJECT_ID = "your-gcp-project-id"

terraform -chdir="infra/terraform/bootstrap" init `
  -backend-config="bucket=$($env:PROJECT_ID)-energy-marketplace-tfstate" `
  -backend-config="prefix=infra/terraform/bootstrap"
```

The state bucket contains Terraform state and may include sensitive generated values. Keep it private and versioned.
