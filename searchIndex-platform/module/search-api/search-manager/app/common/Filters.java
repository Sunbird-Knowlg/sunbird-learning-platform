/**
 * 
 */
package common;

import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.http.HttpFilters;

import javax.inject.Inject;


/**
 * @author pradyumna
 *
 */

public class Filters implements HttpFilters {

    @Inject
    GzipFilter gzipFilter;

    public EssentialFilter[] filters() {
        return new EssentialFilter[] { gzipFilter };
    }
}
