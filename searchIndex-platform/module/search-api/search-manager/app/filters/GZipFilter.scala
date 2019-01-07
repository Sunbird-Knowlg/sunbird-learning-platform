package filters

import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter

class GZipFilter @Inject() extends HttpFilters {
  def filters = Seq(new GzipFilter(shouldGzip = (request, response) =>
    request.headers.get("Accept-Encoding").exists(_.equalsIgnoreCase("gzip"))))
}
