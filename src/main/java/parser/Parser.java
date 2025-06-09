package parser;

import dto.Page;

import java.io.IOException;

public interface Parser {
    Page parsePage(String url);
}
