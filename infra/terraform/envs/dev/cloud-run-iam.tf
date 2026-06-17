resource "google_cloud_run_v2_service_iam_member" "public_invoker" {
  for_each = local.public_cloud_run_services

  project  = var.project_id
  location = var.region
  name     = each.key
  role     = "roles/run.invoker"
  member   = "allUsers"

  depends_on = [
    google_cloud_run_v2_service.api_gateway,
    google_cloud_run_v2_service.billing,
    google_cloud_run_v2_service.listing,
    google_cloud_run_v2_service.trade,
    google_cloud_run_v2_service.user
  ]
}

resource "google_cloud_run_v2_service_iam_member" "service_invoker" {
  for_each = local.private_cloud_run_invokers

  project  = var.project_id
  location = var.region
  name     = each.value.target_service
  role     = "roles/run.invoker"
  member   = "serviceAccount:${local.runtime_service_account_emails[each.value.caller_service]}"

  depends_on = [
    google_cloud_run_v2_service.api_gateway,
    google_cloud_run_v2_service.billing,
    google_cloud_run_v2_service.listing,
    google_cloud_run_v2_service.trade,
    google_cloud_run_v2_service.user
  ]
}
