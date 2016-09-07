CREATE TABLE `indowordnet`.`tbl_holonymy`(  
  `synset_id` INT(11) NOT NULL,
  `holonym_id` INT(11) NOT NULL,
  INDEX `idx_holonymy_01` (`synset_id`),
  INDEX `idx_holonymy_02` (`holonym_id`)
) ENGINE=MYISAM CHARSET=utf8 COLLATE=utf8_general_ci;

CREATE TABLE `indowordnet`.`tbl_antonymy`(  
  `synset_id` INT(11) NOT NULL,
  `antonym_id` INT(11) NOT NULL,
  INDEX `idx_antonymy_01` (`synset_id`),
  INDEX `idx_antonymy_02` (`antonym_id`)
) ENGINE=MYISAM CHARSET=utf8 COLLATE=utf8_general_ci;

CREATE TABLE `indowordnet`.`tbl_meronymy`(  
  `synset_id` INT(11) NOT NULL,
  `meronym_id` INT(11) NOT NULL,
  INDEX `idx_meronymy_01` (`synset_id`),
  INDEX `idx_meronymy_02` (`meronym_id`)
) ENGINE=MYISAM CHARSET=utf8 COLLATE=utf8_general_ci;

CREATE TABLE `indowordnet`.`tbl_action_object`(  
  `synset_id` INT(11) NOT NULL,
  `object_id` INT(11) NOT NULL,
  INDEX `idx_action_object_01` (`synset_id`),
  INDEX `idx_action_object_02` (`object_id`)
) ENGINE=MYISAM CHARSET=utf8 COLLATE=utf8_general_ci;