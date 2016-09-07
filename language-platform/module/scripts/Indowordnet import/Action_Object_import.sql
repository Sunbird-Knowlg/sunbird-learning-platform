INSERT INTO tbl_action_object(synset_id,object_id) SELECT synset_id, function_verb_id FROM tbl_noun_function_verb;
INSERT INTO tbl_action_object(synset_id,object_id) SELECT synset_id, ability_verb_id FROM tbl_noun_ability_verb;
INSERT INTO tbl_action_object(synset_id,object_id) SELECT synset_id, capability_verb_id FROM tbl_noun_capability_verb;

SELECT COUNT(*) FROM tbl_action_object;

SELECT COUNT(*) FROM tbl_noun_function_verb;--46
SELECT COUNT(*) FROM tbl_noun_ability_verb;--19
SELECT COUNT(*) FROM tbl_noun_capability_verb;--5