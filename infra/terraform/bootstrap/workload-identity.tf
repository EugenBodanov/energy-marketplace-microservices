resource "google_iam_workload_identity_pool" "github" {
  workload_identity_pool_id = var.github_wif_pool_id
  display_name              = "GitHub Actions"
  description               = "Federates GitHub Actions into Google Cloud for ${var.github_repository}."

  depends_on = [
    google_project_service.required["iam.googleapis.com"]
  ]
}

resource "google_iam_workload_identity_pool_provider" "github" {
  workload_identity_pool_id          = google_iam_workload_identity_pool.github.workload_identity_pool_id
  workload_identity_pool_provider_id = var.github_wif_provider_id
  display_name                       = "GitHub Actions"
  description                        = "OIDC provider for ${var.github_repository}."

  attribute_condition = "assertion.repository == '${var.github_repository}'"
  attribute_mapping = {
    "google.subject"       = "assertion.sub"
    "attribute.actor"      = "assertion.actor"
    "attribute.repository" = "assertion.repository"
    "attribute.ref"        = "assertion.ref"
    "attribute.workflow"   = "assertion.workflow"
  }

  oidc {
    issuer_uri = "https://token.actions.githubusercontent.com"
  }
}

resource "google_service_account_iam_member" "github_deploy_impersonation" {
  service_account_id = google_service_account.deployer.name
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github.name}/attribute.repository/${var.github_repository}"
}
