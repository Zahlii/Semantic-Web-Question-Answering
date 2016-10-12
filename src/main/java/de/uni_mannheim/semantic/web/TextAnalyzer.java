package de.uni_mannheim.semantic.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

public class TextAnalyzer {
    private static Tokenizer _tokenizer;
    private static Parser _parser;
    private static NameFinderME[] finders;
    
    static {
		try {
			InputStream modelInTokens = new FileInputStream("en-token.bin");
			final TokenizerModel tokenModel = new TokenizerModel(modelInTokens);
			modelInTokens.close();    	 
			_tokenizer = new TokenizerME(tokenModel);
			
			
			InputStream modelInParser = new FileInputStream("en-parser-chunking.bin");
			final ParserModel parseModel = new ParserModel(modelInParser);
			modelInParser.close();
			                
			_parser = ParserFactory.create(parseModel);
		} catch(Exception e) {
			
		}
		
		
	    /*String[] names = {"person", "location", "organization"};
	    int l = names.length;
	    
	    finders = new NameFinderME[l];
	    for (int mi = 0; mi < l; mi++) {
	      finders[mi] = new NameFinderME(new TokenNameFinderModel(
	          new FileInputStream("en-ner-" + names[mi] + ".bin")));
	    }*/
    }
    
    public TextAnalyzer(String text) {
    	this._text = text;
    	this._originalText = _text;
    	
    }
    
    private String _text;
    private String _originalText;
    private HashMap<Integer,String> _variables = new HashMap<Integer,String>();
    
    public void parseSentence() {

		
		Span[] newSpans = replaceNamedEntities();
		ArrayList<String> tokens = tokenizeText(newSpans);
		
		System.out.println(_originalText);
		System.out.println(tokens);
		System.out.println(_variables);
		System.out.println();
		 
		
    }
    
    public void tagSentence() {
    	Span[] spans = _tokenizer.tokenizePos(_text);
    	
		final Parse p = new Parse(_text,new Span(0, _text.length()),AbstractBottomUpParser.INC_NODE,1,0);
		for (int idx=0; idx < spans.length; idx++) {
			final Span span = spans[idx];
			// flesh out the parse with individual token sub-parses 
			p.insert(new Parse(_text,span,AbstractBottomUpParser.TOK_NODE,0,idx));
		}
		
		// https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
		Parse x =_parser.parse(p);
		System.out.println(_originalText);
		x.show();
		System.out.println();
    }
    
    private static boolean isCandidateForEntity(String t) {
    	if(t.contains(" ") || t.contains("_"))
    		return false;
    	String s = t.substring(0,1);
    	return s.toUpperCase().equals(s);
    }

    

	private Span[] replaceNamedEntities() {
		Span[] spans = _tokenizer.tokenizePos(_text);
		
		ArrayList<String> tokens = tokenizeText(spans);
	
		
		int l = tokens.size();
		
		String title;
		
		for(int i=0;i<l;i++) {
			String t = tokens.get(i);
			
			
			String lookup = "";
			String successfulLookup = null;
			String successfulLookupResult = null;
			
			// current token a noun?
			if(isCandidateForEntity(t)) {			
						
				// create up to maxLength Lookups
				for(int j=i;j<Math.min(l,i+4);j++) {
					String nextItem = tokens.get(j);
					lookup = (lookup + " " + nextItem).trim();
					
					// if we found it, it's the new title
					if((title = DBPedia.checkTitleExists(lookup)) != null) {
						successfulLookup = lookup;
						successfulLookupResult = title;
					}
				}
				
				if(successfulLookup != null) {
					int var = _variables.size()+1;
					_variables.put(var, successfulLookupResult);
					_text = _text.replace(successfulLookup, "_"+var);
					return replaceNamedEntities();
				}
			}
		}

		return spans;
	}
	
	private ArrayList<String> tokenizeText(Span[] spans) {
		ArrayList<String> tokens = new ArrayList<String>(spans.length);
		
		for(int i=0,l=spans.length;i<l;i++)
			tokens.add((String) spans[i].getCoveredText(_text));
		
		return tokens;
	}
}
