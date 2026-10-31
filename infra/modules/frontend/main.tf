module "s3_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "5.5.0"

  bucket = "${var.name_prefix}-fe-s3-280769660855"

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false

  attach_policy = true
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "PublicReadGetObject"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = "arn:aws:s3:::${var.name_prefix}-fe-s3-280769660855/*"
      }
    ]
  })

  force_destroy = !var.enable_deletion_protection

  website = {
    index_document = "index.html"
    error_document = "index.html"
  }
}

locals {
  mime_types = {
    "html" = "text/html"
    "css"  = "text/css"
    "js"   = "application/javascript"
    "json" = "application/json"
    "png"  = "image/png"
    "jpg"  = "image/jpeg"
    "jpeg" = "image/jpeg"
    "svg"  = "image/svg+xml"
    "ico"  = "image/x-icon"
    "map"  = "application/json"
    "txt"  = "text/plain"
  }
}

resource "aws_s3_object" "frontend_files" {
  for_each = fileset("${path.root}/../src/frontend/build", "**/*")

  bucket       = module.s3_bucket.s3_bucket_id
  key          = each.value
  source       = "${path.root}/../src/frontend/build/${each.value}"
  etag         = filemd5("${path.root}/../src/frontend/build/${each.value}")
  content_type = lookup(local.mime_types, lower(element(split(".", each.value), length(split(".", each.value)) - 1)), "application/octet-stream")

  depends_on = [module.s3_bucket]
}

# module "cdn" {
#   source = "terraform-aws-modules/cloudfront/aws"
# 
# 
#   comment             = "CloudFront for ${module.s3_bucket.s3_bucket_id} bucket"
#   enabled             = true
#   is_ipv6_enabled     = true
#   price_class         = var.cdn_price_class
#   retain_on_delete    = var.enable_deletion_protection
#   default_root_object = var.default_root_object
# 
#   ### Enable Logging ###
#   # logging_config = {
#   #   bucket = "logs-my-cdn.s3.amazonaws.com"
#   # }
# 
#   origin_access_control = {
#     s3_oac = {
#       description      = "CloudFront access to S3"
#       origin_type      = "s3"
#       signing_behavior = "always"
#       signing_protocol = "sigv4"
#     }
#   }
# 
#   origin = {
#     s3 = {
#       domain_name           = module.s3_bucket.s3_bucket_bucket_regional_domain_name
#       origin_access_control = "s3_oac" # see `origin_access_control`
#     }
#   }
# 
#   default_cache_behavior = {
#     target_origin_id       = "s3" # see`origin`
#     viewer_protocol_policy = "redirect-to-https"
# 
#     allowed_methods = var.cdn_allowed_methods
#     cached_methods  = var.cdn_cached_methods
# 
#     use_forwarded_values = false
# 
#     cache_policy_name          = "Managed-CachingOptimized"
#     origin_request_policy_name = "Managed-CORS-S3Origin"
#   }
# 
#   ordered_cache_behavior = [
#     {
#       target_origin_id       = "s3" # see `origin`
#       path_pattern           = "/static/*"
#       viewer_protocol_policy = "redirect-to-https"
# 
#       allowed_methods = var.cdn_allowed_methods
#       cached_methods  = var.cdn_cached_methods
# 
#       use_forwarded_values = false
# 
#       cache_policy_name            = "Managed-CachingOptimized"
#       origin_request_policy_name   = "Managed-CORS-S3Origin"
#       response_headers_policy_name = "Managed-SimpleCORS"
#     },
#   ]
# 
#   viewer_certificate = {
#     cloudfront_default_certificate = true
#   }
# }
