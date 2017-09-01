CREATE KEYSPACE IF NOT EXISTS unit_test_content_store WITH replication = {
    ‘class’: ‘SimpleStrategy’,
    ‘replication_factor’: ‘1’
};

CREATE TABLE IF NOT EXISTS content_store.content_data (content_id text, last_updated_on timestamp, body blob, oldbody blob, stageIcons blob, PRIMARY KEY (content_id));
