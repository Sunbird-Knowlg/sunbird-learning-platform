package org.sunbird.media.common
/**
  *
  * @author gauraw
  * */
object AzureRequestBody {

  val create_asset = " {\"properties\": {\"description\": \"assetDescription\",\"alternateId\" : \"assetId\"}}"
  val submit_job = "{\"properties\": {\"input\": {\"@odata.type\": \"#Microsoft.Media.JobInputHttp\",\"baseUri\": \"baseInputUrl\",\"files\": [\"inputVideoFile\"]},\"outputs\": [{\"@odata.type\": \"#Microsoft.Media.JobOutputAsset\",\"assetName\": \"assetId\"}]}}"
  val create_stream_locator="{\"properties\":{\"assetName\": \"assetId\",\"streamingPolicyName\": \"policyName\"}}"
}
