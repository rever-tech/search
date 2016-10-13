#@namespace scala rever.rever.search.service

struct TIndexRequest{
    1: required string type,
    2: required string source,
    3: optional string id,
}
service CommonSearch {
    bool index(1: required TIndexRequest indexRequest)
}

