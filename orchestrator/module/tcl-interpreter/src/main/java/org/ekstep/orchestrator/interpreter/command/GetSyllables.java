package org.ekstep.orchestrator.interpreter.command;

import java.util.ArrayList;
import java.util.List;

import org.ekstep.language.measures.entity.Syllable;
import org.ekstep.language.measures.meta.SyllableMap;

import org.ekstep.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class GetSyllables implements ICommand, Command {
	
    static {
        SyllableMap.loadSyllables("te");
        SyllableMap.loadSyllables("hi");
        SyllableMap.loadSyllables("ka");
    }

    @Override
    public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 3) {
            try {
            	TclObject tclObject = argv[1];
                Object obj = ReflectObject.get(interp, tclObject);
                String language = (String) obj;
            	
                tclObject = argv[2];
                obj = ReflectObject.get(interp, tclObject);
                String word = (String) obj;

                List<Syllable> syllables = new ArrayList<Syllable>();
                List<String> syllablesResult = new ArrayList<String>();
                if (null != word && word.trim().length() > 0) {
                    String code = "";
                    List<String> unicodes = new ArrayList<String>();
                    for (int i = 0; i < word.length(); i++) {
                        char ch = word.charAt(i);
                        String uc = String.format("%04x", (int) ch);
                        String s = SyllableMap.getSyllableType(language, uc);
                        if (SyllableMap.CONSONANT_CODE.equalsIgnoreCase(s) || SyllableMap.VOWEL_CODE.equalsIgnoreCase(s)) {
                            if (code.endsWith(SyllableMap.CONSONANT_CODE) || code.endsWith(SyllableMap.VOWEL_CODE)) {
                                if (code.endsWith(SyllableMap.CONSONANT_CODE))
                                    code += SyllableMap.VOWEL_SIGN_CODE;
                                unicodes.add(SyllableMap.getDefaultVowel(language));
                                syllables.add(new Syllable(code, unicodes));
                                code = "";
                                unicodes = new ArrayList<String>();
                            }
                            if (code.endsWith(SyllableMap.HALANT_CODE) && SyllableMap.CONSONANT_CODE.equalsIgnoreCase(s)) {
                                unicodes.add(uc);
                                code += s;
                            } else {
                                unicodes.add(uc);
                                code += s;
                            }
                        } else if (SyllableMap.VOWEL_SIGN_CODE.equalsIgnoreCase(s)) {
                            code += s;
                            unicodes.add(uc);
                            syllables.add(new Syllable(code, unicodes));
                            code = "";
                            unicodes = new ArrayList<String>();
                        } else if (SyllableMap.HALANT_CODE.equalsIgnoreCase(s)) {
                            code += s;
                            unicodes.add(uc);
                        } else if (SyllableMap.CLOSE_VOWEL_CODE.equalsIgnoreCase(s)) {
                            if (code.length() == 0 && syllables.size() > 0) {
                                Syllable syllable = syllables.get(syllables.size() - 1);
                                syllable.setInternalCode(syllable.getInternalCode() + s);
                                syllable.setCode(syllable.getCode() + SyllableMap.VOWEL_CODE);
                                syllable.getUnicodes().add(uc);
                            } else {
                                if (code.endsWith(SyllableMap.CONSONANT_CODE))
                                    code += s;
                                unicodes.add(uc);
                                syllables.add(new Syllable(code, unicodes));
                                code = "";
                                unicodes = new ArrayList<String>();
                            }
                        }
                    }
                    if (code.endsWith(SyllableMap.CONSONANT_CODE) || code.endsWith(SyllableMap.VOWEL_CODE)) {
                        if (code.endsWith(SyllableMap.CONSONANT_CODE)) {
                            code += SyllableMap.VOWEL_SIGN_CODE;
                            unicodes.add(SyllableMap.getDefaultVowel(language));
                        }
                        syllables.add(new Syllable(code, unicodes));
                    } else if (code.endsWith(SyllableMap.HALANT_CODE)) {
                        syllables.add(new Syllable(code, unicodes));
                    }
                }
                for(Syllable syllable: syllables){
                	syllablesResult.add(getUnicodeText(syllable.getUnicodes(), language));
                }
                TclObject tclResp = ReflectObject.newInstance(interp, syllablesResult.getClass(), syllablesResult);
                interp.setResult(tclResp);
            } catch (Exception e) {
                throw new TclException(interp, "Unable to read response: " + e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_syllables_word command");
        }
    }

	private String getUnicodeText(List<String> unicodes, String language) {
		String text = "";
		for(String unicode: unicodes){
			if(!SyllableMap.getDefaultVowel(language).equalsIgnoreCase(unicode)){
			    int hexVal = Integer.parseInt(unicode, 16);
			    text += (char)hexVal;
			}
		}
		return text;
	}

	@Override
    public String getCommandName() {
        return "get_syllables_word";
    }
}
