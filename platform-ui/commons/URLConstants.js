exports.GET_TAXONOMIES = '/taxonomy';
exports.GET_TAXONOMY = '/taxonomy/${id}';
exports.GET_CONCEPT_TAXONOMY_DEFS = '/taxonomy/${id}/definition/Concept';
exports.GET_GAME_TAXONOMY_DEFS = '/taxonomy/${id}/definition/Game';
exports.GET_CONCEPT = '/concept/${id}';
exports.SAVE_CONCEPT = '/concept?taxonomyId=${tid}';
exports.UPDATE_CONCEPT = '/concept/${id}?taxonomyId=${tid}';