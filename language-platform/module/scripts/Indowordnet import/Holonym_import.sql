INSERT INTO tbl_holonymy(synset_id, holonym_id) SELECT synset_id, holo_component_object_id FROM tbl_noun_holo_component_object;
INSERT INTO tbl_holonymy(synset_id, holonym_id) SELECT synset_id, holo_feature_activity_id FROM tbl_noun_holo_feature_activity;
INSERT INTO tbl_holonymy(synset_id, holonym_id) SELECT synset_id, holo_member_collection_id FROM tbl_noun_holo_member_collection;
INSERT INTO tbl_holonymy(synset_id, holonym_id) SELECT synset_id, holo_phase_state_id FROM tbl_noun_holo_phase_state;
INSERT INTO tbl_holonymy(synset_id, holonym_id) SELECT synset_id, holo_place_area_id FROM tbl_noun_holo_place_area;
INSERT INTO tbl_holonymy(synset_id, holonym_id) SELECT synset_id, holo_portion_mass_id FROM tbl_noun_holo_portion_mass;
INSERT INTO tbl_holonymy(synset_id, holonym_id) SELECT synset_id, holo_position_area_id FROM tbl_noun_holo_position_area;
INSERT INTO tbl_holonymy(synset_id, holonym_id) SELECT synset_id, holo_resource_process_id FROM tbl_noun_holo_resource_process;
INSERT INTO tbl_holonymy(synset_id, holonym_id) SELECT synset_id, holo_stuff_object_id FROM tbl_noun_holo_stuff_object;

SELECT COUNT(*) FROM tbl_holonymy;

SELECT * FROM tbl_holonymy;