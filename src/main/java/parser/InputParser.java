package parser;

import java.util.Arrays;
import java.util.HashSet;

public class InputParser {
    private final String[] arguments;
    private final Parser parser=new Parser();
    private final ParsedInputArguments parsedArguments;

    public InputParser(String[] args) {
        this.arguments = args;
        this.parsedArguments = new ParsedInputArguments();
    }

    /**
     * This method takes a String array as input and compares it to the program-calling-conventions.
     * If the argument array fits all the requirements and conventions, the information required
     * for the crawler, a ParsedInputArguments object is filled with all the
     * relevant data and then returned.
     * @return A ParsedInputArguments object, containing only the necessary information required
     * for the crawler to work.
     */
    public ParsedInputArguments parseInputArguments() {
        try {
            areNumberOfArgumentsValid();
            setURLFromInputArgument();
            setDepthFromInputArgument();
            setDomainsFromInputArgument();
            checkStartUrlMatchedDomains();
            return parsedArguments;
        }catch (IllegalArgumentException e){
            throw e;
        }
    }




    private void setURLFromInputArgument() throws IllegalArgumentException{
        isURLArgumentValid();
        parsedArguments.setStartUrl(this.arguments[0]);
    }

    private void setDepthFromInputArgument() throws IllegalArgumentException{
        isDepthArgumentValid();
        parsedArguments.setMaxDepth(Integer.parseInt(this.arguments[1]));

    }

    private void setDomainsFromInputArgument() throws IllegalArgumentException {
        isDomainsArgumentValid();
        parsedArguments.setAllowedDomains( new HashSet<>(Arrays.asList(arguments).subList(2, arguments.length)));

    }

    private void isDomainsArgumentValid() throws IllegalArgumentException{
        for (int i = 2; i < arguments.length; i++) {
            if(!isValidDomain(arguments[i])) throw new IllegalArgumentException("Domain not Valid");
        }
    }
    // Validate a domain string (simple pattern check created by ChatGPT)
    private boolean isValidDomain(String domain) {
        return domain.matches("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }


    private void isURLArgumentValid() throws IllegalArgumentException {
        if(!parser.isValidLink(this.arguments[0])){
            throw new IllegalArgumentException("Url Input argument is broken!");
        }
    }

    private void isDepthArgumentValid() throws IllegalArgumentException {
        try{
            int parseIntTest = Integer.parseInt(this.arguments[1]);
        }catch (NumberFormatException nfe){
            throw new IllegalArgumentException("Please enter an integer number as depth input");
        }
    }

    private void checkStartUrlMatchedDomains() throws IllegalArgumentException{
        for (String domain: parsedArguments.getAllowedDomains()){
            if(parsedArguments.getStartUrl().contains(domain))return;
        }
        throw new IllegalArgumentException("StartUrl is not withing allowed Domains");
    }

    private void areNumberOfArgumentsValid() throws IllegalArgumentException {
        if(this.arguments.length<3){
            throw new IllegalArgumentException
                    ("""
                            The number of arguments is invalid
                            Please make sure to use this format:
                            <URL> <depth how far the program crawls> <any number of domains you want to crawl>""");
        }
    }
}



