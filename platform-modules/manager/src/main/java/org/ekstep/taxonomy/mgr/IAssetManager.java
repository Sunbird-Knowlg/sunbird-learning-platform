package org.ekstep.taxonomy.mgr;

import org.ekstep.common.dto.Response;

import java.util.Map;

/**
 * The interface <code>IAssetManager</code> is contract for all asset related operations.
 *
 * @see org.ekstep.taxonomy.mgr.impl.AssetManagerImpl
 */
public interface IAssetManager {

    /**
     *
     * @param asset
     * @return
     * @throws Exception
     */
    Response licenseValidate(Map<String, Object> asset) throws Exception;

    /**
     *
     * @param asset
     * @return
     * @throws Exception
     */
    Response metadataRead(Map<String, Object> asset) throws Exception;
}
