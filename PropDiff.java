// PropDiff
// See Copyright notice below.

import java.io.*;
import java.util.Properties;
import java.util.Enumeration;

/** 
 * PropDiff is a utility for comparing and combining property files
 * that can find the union, intersection, and difference. 
 * See <a href="http://java.sun.com/j2se/1.4/docs/api/java/util/Properties.html">java.util.Properties</a>
 * for more information about property files.<p>
 * Run this with no arguments to see the usage/help that looks like:
 * <pre>
Usage: [flags] properties-file1 properties-file2 [-f filenameOrPathPrefixForResults]
  flags:
    -c  property settings that are common to both p1 and p2, where p2 take precedence
    -u  union p1 and p2 where p2 has higher precedence
    -1  properties settings that are only in p1
    -2  properties settings that are only in p2
    -d  intersection of properties in p1 and p2 that have different values
    -e  intersection of properties in p1 and p2 that have equal values

  The flags can either be combined or separate.
  When no flags are used, all 6 property file combinations are produced.

  Use:  -f -  
    to stream output to console (stdout)

  Usage Examples:
    dump all variants to console:
      p1.properties p2.properties -f -
    combine two property files where first is default:
      -u p1.properties p2.properties
  Input property files are not modified.
</pre>
 * @author peb@baclace.net
 * @version 1.0
 * @see <a href="http://www.baclace.net/java/prop_diff">PropDiff home</a>
 * <DT><B>License:</B><DD>
 * <i><pre>
 * Copyright (C) 2001 Step.com Communications, Inc.
 * Copyright (C) 2002 Paul E. Baclace 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE. </pre></i></DD>
 *  
 */
public class PropDiff {
	//
	// Instance variables
	//
	private Properties p1;
	private Properties p2;
	private String filenamep1;
	private String filenamep2;
	private boolean toStdout = false;

	//
	// Constructor
	//

	/** Create a PropDiff that will operate on the Properties objects p1 and p2.
	 * Entries in p2 take precedence over p1 (that is, p1 is the default of p2). 
	 * The filenames are used when generating comments and as a prefix when 
	 * creating output files and are neither modified nor read here. 
	 * @param p1 a default or lower precedence Properties object.
	 * @param p2 a higher precedence Properties object than p1.
	 * @param filenamep1 filename (with or without full path) corresponding 
	 * to p1.  
	 * @param filenamep2 filename (with or without full path) corresponding 
	 * to p2. 
	 * @param toStdout if true, send results to System.out instead of creating
	 * property files as output.
	 */
	public PropDiff(Properties p1, String filenamep1, Properties p2, 
			String filenamep2, boolean toStdout) {
		this.p1 = p1;
		this.p2 = p2;
		this.filenamep1 = filenamep1;
		this.filenamep2 = filenamep2;
		this.toStdout = toStdout;
	}

	//
	// Class variables
	//
	private static String eol = System.getProperty("line.separator");
	private static String usage =  
		"Usage: [flags] properties-file1 properties-file2 [-f filenameOrPathPrefixForResults]" +
		eol +
		"  flags:" +
		eol +
		"    -c  property settings that are common to both p1 and p2, where p2 take precedence" +
		eol +
		"    -u  union p1 and p2 where p2 has higher precedence" +
		eol +
		"    -1  properties settings that are only in p1" +
		eol +
		"    -2  properties settings that are only in p2" +
		eol +
		"    -d  intersection of properties in p1 and p2 that have different values" +
		eol +
		"    -e  intersection of properties in p1 and p2 that have equal values" +
		eol +
		"  The flags can either be combined or separate." +
		eol +
		"  When no flags are used, all 6 property file combinations are produced." +
		eol +
		"  Use:  -f -  " + 
		eol + 
		"    to stream output to console (stdout)" +
		eol +
		"  Usage Examples:" + 
		eol +  
		"    dump all variants to console:" +
		eol +
		"      p1.properties p2.properties -f -" +
		eol +  
		"    combine two property files where first is default:" +
		eol +
		"      -u p1.properties p2.properties" +
		eol +
		"  Input property files are not modified.";
	

	/** Emit the usage to System.err and then exit.
	 */
	private static void emitUsageAndQuit(String message) {
		System.err.println("" + message);
		System.err.println(usage);
		System.exit(1);
	}

	/** Return true if given char is in given String.
	 * This is mostly here for readability.
	 */
	private static boolean performFlagAction(String sflags, char c) {
		return ((sflags.length() == 0) || (sflags.indexOf(c) >= 0));
	}
	
	/** The main method with a commandline interface.
	 * Run this with no arguments to see the usage/help.
	 */
	public static void main(String[] args) {
		String flags = "";
		String filePrefix = "";
		String sp1 = null;
		String sp2 = null;
		boolean toStdout = false;

		if ((args.length < 2) || (args.length > 4)) {
			emitUsageAndQuit("");
		} else {
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if (arg.equals("-f")) {
					if ((i + 1) < args.length) {
						filePrefix = args[++i];
						if ("-".equals(filePrefix)) {
							toStdout = true;
							System.out.println("#Results go to console (stdout)"); 
						} else {
							System.out.println("filename Path Prefix For Results = " + 
									filePrefix);
						}
					} else {
						emitUsageAndQuit("expected filenameOrPath after -f");
					}
				} else if (arg.startsWith("-")) {
					flags += arg.substring(1).toLowerCase();
				} else if (sp1 == null) {
					sp1 = args[0];
				} else if (sp2 == null) {
					sp2 = args[1];
				} else {
					emitUsageAndQuit("unexpected arg: " + arg);
				}
			}

			PropDiff pdiff = 
				new PropDiff(PropDiff.loadByFilename(sp1), sp1,
						PropDiff.loadByFilename(sp2), sp2, toStdout);

			if (performFlagAction(flags, 'e')) {
				pdiff.saveByFilename(pdiff.intersect(true), 
						filePrefix + "intersectEqual.properties", 
						" intersection showing values that are equal for " + sp1 + 
						"  and  " + sp2);
			}
			if (performFlagAction(flags, 'd')) {
				pdiff.saveByFilename(pdiff.intersect(false), 
						filePrefix + "intersectDiff.properties", 
						" intersection showing properties in " + sp2 + 
						" that override property values in " + sp1);
			}
			if (performFlagAction(flags, 'c')) {
				pdiff.saveByFilename(pdiff.commonProps(), 
						filePrefix + "common.properties", 
						" intersection of " + sp1 + 
						"  and  " + sp2  +
						" where the latter takes precedence if values differ.");
			}
			if (performFlagAction(flags, '2')) {
				pdiff.saveByFilename(pdiff.onlyIn(2), 
						filePrefix + "onlyInP2.properties", 
						" properties in " + sp2 + " that are not present in " + sp1);
			}
			if (performFlagAction(flags, '1')) {
				pdiff.saveByFilename(pdiff.onlyIn(1), 
						filePrefix + "onlyInP1.properties", 
						" properties in " + sp1 + " that are not present in " + sp2);
			}
			if (performFlagAction(flags, 'u')) {
				pdiff.saveByFilename(pdiff.union(), 
						filePrefix + "union.properties", 
						" union of " + sp1 + " and " + sp2 + 
						" where the latter has precedence if values differ");
			}
			System.exit(0);
		}
	}

	/** Return properties that have the same name and equal or different values.
	 * The returned properties that are common (same name) with or without the 
	 * same values, depending on intersectValues.  (p1 is default for p2).
	 * @param intersectValues if true, then return props that also have the 
	 * same value.  Otherwise, return only properties that have different values.
	 * @return properties in p1 and p2 that have the same name with the
	 * added condition intersectValues. 
	 */
	public Properties intersect(boolean intersectValues) {
		Properties props = new Properties();
		// scan p1
		for (Enumeration e = p1.propertyNames(); e.hasMoreElements();) {
			String name = (String)e.nextElement();
			String value1 = p1.getProperty(name);
			String value2 = p2.getProperty(name);
			if (value2 != null) { // name is in both
				if (intersectValues) {
					if (value1.equals(value2)) {  // same value
						props.put(name, value1);
					} 
				} else { 
					if (! value1.equals(value2)) {  // different value
						props.put(name, value2);
					} 
				}
			}
		}
		return props;
	}

	/** Return properties that are common (same name).
	 * The returned properties that are common to p1 and p2, where p1 is default 
	 * for p2 (said another way, p2 overrides p1).
	 * @return properties that are in both p1 and p2 where p1 is default for p2.
	 */
	public Properties commonProps() {
		Properties props = new Properties();
		// scan p2
		for (Enumeration e = p2.propertyNames(); e.hasMoreElements();) {
			String name = (String)e.nextElement();
			String value2 = p2.getProperty(name);
			String value1 = p1.getProperty(name);
			if (value1 != null) { // in both
				props.put(name, value2); // value from p2 overrides p1
			}
		}
		return props;
	}

 	/** Return union of properties.
		@return union of properties, where p2 overrides p1
	 */
	public Properties union() {
		Properties props = new Properties();
		// scan p1
		for (Enumeration e = p1.propertyNames(); e.hasMoreElements();) {
			String name = (String)e.nextElement();
			String value = p1.getProperty(name);
			props.put(name, value);
		}
		// scan p2
		for (Enumeration e = p2.propertyNames(); e.hasMoreElements();) {
			String name = (String)e.nextElement();
			String value = p2.getProperty(name);
			props.put(name, value);
		}
		return props;
	}
 
	/** Return properties only in one of the Properties objects.
	 * The values of property items are not considered in this case.
	 * @param which either 1 or 2.
	 * @return properties only in one of the Properties objects.
	 */
	public Properties onlyIn(int which) {
		Properties props = new Properties();
		Properties pp1 = null;
		Properties pp2 = null;
		if (which == 2) {		
			pp1 = p1;
			pp2 = p2;
		} else {
			pp1 = p2;
			pp2 = p1;
		}
		// scan pp2
		for (Enumeration e = pp2.propertyNames(); e.hasMoreElements();) {
			String name = (String)e.nextElement();
			String value2 = pp2.getProperty(name);
			String value1 = pp1.getProperty(name);
			if (value1 == null) { // only in pp2
				props.put(name, value2); 
			}
		}
		return props;
	}
 
	/** Load a saved Properties object by filename.
	 */
	private static Properties loadByFilename(String filename) {
		Properties props = new Properties();
		BufferedInputStream bis;
		try {
			bis = new BufferedInputStream(new FileInputStream(filename));
			props.load(bis);
		} catch (IOException e) { // either not found or io error
			System.err.println("when opening " + filename + " : " + e);
			return null;
		}
		return props;
	}

	/** Save a Properties object by filename, with given comment.
	 */
	private void saveByFilename(Properties props, String filename, String comment) {
		OutputStream bos;
		if (props != null) {
				try {
					if (toStdout) {
						bos = System.out;
					} else {
						bos = new BufferedOutputStream(new FileOutputStream(filename));
					}
					props.store(bos, comment);
					if (toStdout) {
						System.out.println();
					} else {
						bos.close();
					}
				} catch (IOException e) { // either not found or io error
					System.err.println("when saving to " + filename + " : " + e);
				}
			
		}
	}

}

