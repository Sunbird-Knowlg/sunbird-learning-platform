// Scripts to return content count for specific condition 

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.medium) and exists(n.topic) and exists(n.gradeLevel) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) and not exists(n.se_topics) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.medium) and exists(n.gradeLevel) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.medium) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_mediums) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.gradeLevel) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_gradeLevels) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.subject) and exists(n.medium) and exists(n.topic) and exists(n.gradeLevel) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) and not exists(n.se_topics) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.subject) and exists(n.medium) and exists(n.gradeLevel) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.subject) and exists(n.gradeLevel) AND not exists(n.se_subjects) and not exists(n.se_gradeLevels) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.gradeLevel) AND not exists(n.se_boards) and not exists(n.se_gradeLevels) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.board) and not exists(n.se_boards) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.subject) and not exists(n.se_subjects) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.medium) and not exists(n.se_mediums) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.gradeLevel) and not exists(n.se_gradeLevels) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.topic) and not exists(n.se_topics) return count(n);

// Scripts to set content metadata for org and target framework based on specific condition.

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.medium) and exists(n.topic) and exists(n.gradeLevel) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) and not exists(n.se_topics) set n.se_boards=[n.board], n.se_subjects=n.subject, n.se_mediums=n.medium, n.se_gradeLevels=n.gradeLevel, n.se_topics=n.topic;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.medium) and exists(n.gradeLevel) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) set n.se_boards=[n.board], n.se_subjects=n.subject, n.se_mediums=n.medium, n.se_gradeLevels=n.gradeLevel;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.medium) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_mediums) set n.se_boards=[n.board], n.se_subjects=n.subject, n.se_mediums=n.medium;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.gradeLevel) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_gradeLevels) set n.se_boards=[n.board], n.se_subjects=n.subject, n.se_gradeLevels=n.gradeLevel;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.subject) and exists(n.medium) and exists(n.topic) and exists(n.gradeLevel) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) and not exists(n.se_topics) set n.se_subjects=n.subject, n.se_mediums=n.medium, n.se_gradeLevels=n.gradeLevel, n.se_topics=n.topic;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.subject) and exists(n.medium) and exists(n.gradeLevel) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) set n.se_subjects=n.subject, n.se_mediums=n.medium, n.se_gradeLevels=n.gradeLevel;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.subject) and exists(n.gradeLevel) AND not exists(n.se_subjects) and not exists(n.se_gradeLevels) set n.se_subjects=n.subject, n.se_gradeLevels=n.gradeLevel;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.gradeLevel) AND not exists(n.se_boards) and not exists(n.se_gradeLevels) set n.se_boards=[n.board], n.se_gradeLevels=n.gradeLevel;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.board) and not exists(n.se_boards) set n.se_boards=[n.board];

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.subject) and not exists(n.se_subjects) set n.se_subjects=n.subject;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.medium) and not exists(n.se_mediums) set n.se_mediums=n.medium;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.gradeLevel) and not exists(n.se_gradeLevels) set n.se_gradeLevels=n.gradeLevel;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Content'] and n.status in ['Live', 'Unlisted'] and exists(n.topic) and not exists(n.se_topics) set n.se_topics=n.topic;



// Scripts to return collection (non Course) count for specific condition 

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.medium) and exists(n.topic) and exists(n.gradeLevel) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) and not exists(n.se_topics) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.medium) and exists(n.gradeLevel) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.gradeLevel) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_gradeLevels) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.subject) and exists(n.medium) and exists(n.topic) and exists(n.gradeLevel) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) and not exists(n.se_topics) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.subject) and exists(n.medium) and exists(n.gradeLevel) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.subject) and exists(n.gradeLevel) AND not exists(n.se_subjects) and not exists(n.se_gradeLevels) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.medium) and exists(n.gradeLevel) AND not exists(n.se_mediums) and not exists(n.se_gradeLevels) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.board) and not exists(n.se_boards) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.subject) and not exists(n.se_subjects) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.medium) and not exists(n.se_mediums) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.gradeLevel) and not exists(n.se_gradeLevels) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.topic) and not exists(n.se_topics) return count(n);

// Scripts to set collection (non Course) metadata for org and target framework based on specific condition.

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.medium) and exists(n.topic) and exists(n.gradeLevel) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) and not exists(n.se_topics) set n.se_boards=[n.board], n.se_subjects=n.subject, n.se_mediums=n.medium, n.se_gradeLevels=n.gradeLevel, n.se_topics=n.topic;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.medium) and exists(n.gradeLevel) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) set n.se_boards=[n.board], n.se_subjects=n.subject, n.se_mediums=n.medium, n.se_gradeLevels=n.gradeLevel;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.board) and exists(n.subject) and exists(n.gradeLevel) and not exists(n.se_boards) AND not exists(n.se_subjects) and not exists(n.se_gradeLevels) set n.se_boards=[n.board], n.se_subjects=n.subject, n.se_gradeLevels=n.gradeLevel;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.subject) and exists(n.medium) and exists(n.topic) and exists(n.gradeLevel) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) and not exists(n.se_topics) set n.se_subjects=n.subject, n.se_mediums=n.medium, n.se_gradeLevels=n.gradeLevel, n.se_topics=n.topic;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.subject) and exists(n.medium) and exists(n.gradeLevel) AND not exists(n.se_subjects) and not exists(n.se_mediums) and not exists(n.se_gradeLevels) set n.se_subjects=n.subject, n.se_mediums=n.medium, n.se_gradeLevels=n.gradeLevel;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.subject) and exists(n.gradeLevel) AND not exists(n.se_subjects) and not exists(n.se_gradeLevels) set n.se_subjects=n.subject, n.se_gradeLevels=n.gradeLevel;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.medium) and exists(n.gradeLevel) AND not exists(n.se_mediums) and not exists(n.se_gradeLevels) set n.se_mediums=n.medium, n.se_gradeLevels=n.gradeLevel;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.board) and not exists(n.se_boards) set n.se_boards=[n.board];

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.subject) and not exists(n.se_subjects) set n.se_subjects=n.subject;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.medium) and not exists(n.se_mediums) set n.se_mediums=n.medium;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.gradeLevel) and not exists(n.se_gradeLevels) set n.se_gradeLevels=n.gradeLevel;

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection'] and n.contentType<>'Course' and n.status in ['Live', 'Unlisted'] and exists(n.topic) and not exists(n.se_topics) set n.se_topics=n.topic;

// Scripts to return collection count where objectType exists

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection', 'CollectionImage'] and exists(n.objectType) return n.objectType, count(n);

// Scripts to nullify objectType value for collection objects

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['Collection', 'CollectionImage'] and exists(n.objectType) remove n.objectType;

//Script to update framework with default systemDefault value as No

match (n:domain) where n.IL_FUNC_OBJECT_TYPE='Framework' and not exists (n.systemDefault) return count(n);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE='Framework' and not exists (n.systemDefault) set n.systemDefault = 'No';

// Scripts to set compatibilityLevel=5 for all QuestionSet
match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['QuestionSet', 'QuestionSetImage'] and exists(n.compatibilityLevel) return n.compatibilityLevel, count(n.compatibilityLevel);

match (n:domain) where n.IL_FUNC_OBJECT_TYPE in ['QuestionSet', 'QuestionSetImage'] and exists(n.compatibilityLevel) set n.compatibilityLevel=5;