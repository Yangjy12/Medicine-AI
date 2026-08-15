package com.xinglin.video.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VideoSearchRepository extends ElasticsearchRepository<VideoSearchDocument, Long> {
}
