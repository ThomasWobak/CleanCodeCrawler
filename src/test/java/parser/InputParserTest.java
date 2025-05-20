package parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InputParserTest {

    private String[] validArguments;
    private String[] invalidArguments;
    private String[] customArguments;
    private InputParser inputArgumentParser;

    @BeforeEach
    public void initializeValidArgumentsString(){
        validArguments= new String[]{
                "http://gilead-verein.at",
                "2",
                "gilead-verein.at",
                "hivegames.at"
        };
    }

    @BeforeEach
    public void initializeInvalidArgumentsString(){
        invalidArguments= new String[]{
                "invalidLinkExample",
                "invalidDepth",
                "invalidDomain"
        };
    }

    @BeforeEach
    public void initializeCustomArgumentsString(){
        customArguments=validArguments.clone();

    }

    @Test
    public void testParseInputArgumentsValid() {
        inputArgumentParser = new InputParser(validArguments);
        Assertions.assertDoesNotThrow(
                () -> inputArgumentParser.parseInputArguments());
    }

    @Test
    public void testParseInputArgumentsInvalid() {
        inputArgumentParser = new InputParser(new String[]{});
        Assertions.assertThrows(
                Exception.class,
                () -> inputArgumentParser.parseInputArguments());
    }

    @Test
    public void testAreNumberOfArgumentsValidError(){
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
    public void testIsURLArgumentValidError(){
        customArguments[0]=invalidArguments[0];
        inputArgumentParser = new InputParser(customArguments);

        IllegalArgumentException invalidURLArgumentTagException = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> inputArgumentParser.parseInputArguments());

        String expectedErrorMessage=("Url Input argument is broken!");
        Assertions.assertEquals(expectedErrorMessage, invalidURLArgumentTagException.getMessage());
    }


    @Test
    public void testIsDepthArgumentValid_NonIntegerError() {
        customArguments[1] = invalidArguments[1];
        inputArgumentParser = new InputParser(customArguments);

        IllegalArgumentException nonIntegerInput = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> inputArgumentParser.parseInputArguments());

        String expectedErrorMessage = ("Please enter an integer number as depth input");
        Assertions.assertEquals(expectedErrorMessage, nonIntegerInput.getMessage());
    }
    @Test
    public void testIsDomainArgumentValid_InvalidDomain(){
        customArguments[2] = invalidArguments[2];
        inputArgumentParser = new InputParser(customArguments);

        IllegalArgumentException invalidDomain = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> inputArgumentParser.parseInputArguments());

        String expectedErrorMessage = ("Domain not Valid");
        Assertions.assertEquals(expectedErrorMessage, invalidDomain.getMessage());
    }
    @Test
    public void testIsDomainArgumentValid_StartUrlNotInDomain(){
        customArguments[0] = "https://google.at";
        inputArgumentParser = new InputParser(customArguments);

        IllegalArgumentException invalidDomain = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> inputArgumentParser.parseInputArguments());

        String expectedErrorMessage = ("StartUrl is not withing allowed Domains");
        Assertions.assertEquals(expectedErrorMessage, invalidDomain.getMessage());
    }
}
