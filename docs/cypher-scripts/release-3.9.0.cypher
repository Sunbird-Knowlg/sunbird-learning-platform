// Scripts to set visibility=Defualt for all ObjectCategoryDefinition objects.

MATCH (n:domain{IL_FUNC_OBJECT_TYPE:"ObjectCategoryDefinition"}) SET n.visibility="Default";

// Scripts to set  visibility=Parent for few specific ObjectCategoryDefinition objects.

MATCH (n:domain{IL_FUNC_OBJECT_TYPE:"ObjectCategoryDefinition"}) WHERE n.IL_UNIQUE_ID IN ["obj-cat:course-unit_collection_all","obj-cat:textbook-unit_collection_all","obj-cat:lesson-plan-unit_collection_all","obj-cat:certificate-template_asset_all","obj-cat:plugin_content_all","obj-cat:template_content_all"] SET n.visibility="Private";