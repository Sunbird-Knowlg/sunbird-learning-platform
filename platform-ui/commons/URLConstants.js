exports.GET_TAXONOMIES = '/taxonomy-service/taxonomy';
exports.GET_TAXONOMY = '/taxonomy-service/taxonomy/${id}';
exports.GET_CONCEPT_TAXONOMY_DEFS = '/taxonomy-service/taxonomy/${id}/definition/Concept';
exports.GET_GAME_TAXONOMY_DEFS = '/taxonomy-service/taxonomy/${id}/definition/Game';
exports.GET_CONCEPT = '/taxonomy-service/concept/${id}';
exports.SAVE_CONCEPT = '/taxonomy-service/concept?taxonomyId=${tid}';
exports.UPDATE_CONCEPT = '/taxonomy-service/concept/${id}?taxonomyId=${tid}';