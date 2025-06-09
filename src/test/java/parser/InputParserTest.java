package parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
class InputParserTest {

    private String[] validArguments;
    private String[] invalidArguments;
    private String[] customArguments;
    private InputParser inputArgumentParser;

    @BeforeEach
    protected void initializeValidArgumentsString(){
        validArguments= new String[]{
                "http://gilead-verein.at",
                "2",
                "gilead-verein.at",
                "hivegames.at"
        };
    }

    @BeforeEach
    protected void initializeInvalidArgumentsString(){
        invalidArguments= new String[]{
                "invalidLinkExample",
                "invalidDepth",
                "invalidDomain"
        };
    }

    @BeforeEach
    protected void initializeCustomArgumentsString(){
        customArguments=validArguments.clone();

    }

    @Test
    protected void testParseInputArgumentsValid() {
        inputArgumentParser = new InputParser(validArguments);
        Assertions.assertDoesNotThrow(
                () -> inputArgumentParser.parseInputArguments());
    }

    @Test
    protected void testParseInputArgumentsInvalid() {
        inputArgumentParser = new InputParser(new String[]{});
        Assertions.assertThrows(
                Exception.class,
                () -> inputArgumentParser.parseInputArguments());
    }

    @Test
    protected void testAreNumberOfArgumentsValidError(){
        inputArgumentParser = new InputParser(new String[]{});
        IllegalArgumentException numberOfArgumentsException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> inputArgumentParser.parseInputArguments());

        String expectedErrorMessage=("""
                            The number of arguments is invalid
                            Please make sure to use this format:
                            <URL> <depth how far the program crawls> <any number of domains you want to crawl>""");

        Assertions.assertEquals(expectedErrorMessage, numberOfArgumentsException.getMessage());
    }



    @Test
    protected void testIsURLArgumentValidError(){
        customArguments[0]=invalidArguments[0];
        inputArgumentParser = new InputParser(customArguments);

        IllegalArgumentException invalidURLArgumentTagException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> inputArgumentParser.parseInputArguments());

        String expectedErrorMessage=("Url Input argument is broken!");
        Assertions.assertEquals(expectedErrorMessage, invalidURLArgumentTagException.getMessage());
    }


    @Test
    protected void testIsDepthArgumentValid_NonIntegerError() {
        customArguments[1] = invalidArguments[1];
        inputArgumentParser = new InputParser(customArguments);

        IllegalArgumentException nonIntegerInput = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> inputArgumentParser.parseInputArguments());

        String expectedErrorMessage = ("Please enter an integer number as depth input");
        Assertions.assertEquals(expectedErrorMessage, nonIntegerInput.getMessage());
    }
    @Test
    protected void testIsDomainArgumentValid_InvalidDomain(){
        customArguments[2] = invalidArguments[2];
        inputArgumentParser = new InputParser(customArguments);

        IllegalArgumentException invalidDomain = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> inputArgumentParser.parseInputArguments());

        String expectedErrorMessage = ("Domain not Valid");
        Assertions.assertEquals(expectedErrorMessage, invalidDomain.getMessage());
    }
    @Test
    protected void testIsDomainArgumentValid_StartUrlNotInDomain(){
        customArguments[0] = "https://google.at";
        inputArgumentParser = new InputParser(customArguments);

        IllegalArgumentException invalidDomain = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> inputArgumentParser.parseInputArguments());

        String expectedErrorMessage = ("StartUrl is not withing allowed Domains");
        Assertions.assertEquals(expectedErrorMessage, invalidDomain.getMessage());
    }
}
 */
