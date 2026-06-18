resource "google_service_account" "deployer" {
  account_id   = var.deploy_service_account_id
  display_name = "Energy Marketplace GitHub deployer"
  description  = "Used by GitHub Actions to push images and apply Terraform-managed Cloud Run changes."

  depends_on = [
    google_project_service.required["iam.googleapis.com"]
  ]
}

resource "google_project_iam_member" "deployer_project_roles" {
  for_each = local.deployer_project_roles

  project = var.project_id
  role    = each.key
  member  = "serviceAccount:${google_service_account.deployer.email}"
}

resource "google_service_account" "runtime" {
  for_each = local.runtime_service_accounts

  account_id   = each.value.account_id
  display_name = each.value.display_name
  description  = "Runtime identity for ${each.key}."

  depends_on = [
    google_project_service.required["iam.googleapis.com"]
  ]
}
