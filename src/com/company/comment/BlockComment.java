package com.company.comment;

public class BlockComment {
    private final static String OPEN_BLOCK_COMMENT = "/*";
    private final static String CLOSE_BLOCK_COMMENT = "*/";

    private boolean isOpenBlockComment;

    public BlockComment() {
        this.isOpenBlockComment = false;
    }

    public boolean checkIfCanCloseComment(String nextCharacter, String token) {
        String symbol =  token.trim() + nextCharacter;
        if (symbol.equals(CLOSE_BLOCK_COMMENT)) {
            isOpenBlockComment = false;
        }
        return !isOpenBlockComment;
    }

    public boolean checkIfOpenComment(String nextCharacter, String token) {
        String blockCommentTest =  token + nextCharacter;
        if (blockCommentTest.equals(OPEN_BLOCK_COMMENT)) {
            isOpenBlockComment = true;
        }
        return isOpenBlockComment;
    }

    public boolean isOpen() {
        return isOpenBlockComment;
    }
}
