
import processor.SampleProcessorDriver;

/**
 * Convenient driver to test samples.
 * 
 * @author Kohsuke Kawaguchi
 */
public class SampleRunner {
    public static void main(String[] args) {
        if(args.length==0) {
            System.out.println(
                    "Usage: SampleRunner <sampleDir1> <sampleDir2> ... \n"+
                    "Runs samples specified in the command line");
        }

        for( String dir : args )
            SampleProcessorDriver.main("-dir",dir,"-validating","-execute","-verbose","-ant","workspace");
    }
}
