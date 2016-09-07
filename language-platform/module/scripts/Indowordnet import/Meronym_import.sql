INSERT INTO tbl_meronymy(synset_id, meronym_id) SELECT synset_id, mero_component_object_id FROM tbl_noun_mero_component_object;
INSERT INTO tbl_meronymy(synset_id, meronym_id) SELECT synset_id, mero_feature_activity_id FROM tbl_noun_mero_feature_activity;
INSERT INTO tbl_meronymy(synset_id, meronym_id) SELECT synset_id, mero_member_collection_id FROM tbl_noun_mero_member_collection;
INSERT INTO tbl_meronymy(synset_id, meronym_id) SELECT synset_id, mero_phase_state_id FROM tbl_noun_mero_phase_state;
INSERT INTO tbl_meronymy(synset_id, meronym_id) SELECT synset_id, mero_place_area_id FROM tbl_noun_mero_place_area;
INSERT INTO tbl_meronymy(synset_id, meronym_id) SELECT synset_id, mero_portion_mass_id FROM tbl_noun_mero_portion_mass;
INSERT INTO tbl_meronymy(synset_id, meronym_id) SELECT synset_id, mero_position_area_id FROM tbl_noun_mero_position_area;
INSERT INTO tbl_meronymy(synset_id, meronym_id) SELECT synset_id, mero_resource_process_id FROM tbl_noun_mero_resource_process;
INSERT INTO tbl_meronymy(synset_id, meronym_id) SELECT synset_id, mero_stuff_object_id FROM tbl_noun_mero_stuff_object;

SELECT COUNT(*) FROM tbl_meronymy;