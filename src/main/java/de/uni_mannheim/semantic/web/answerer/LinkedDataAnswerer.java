package de.uni_mannheim.semantic.web.answerer;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.stanford_nlp.model.Answer;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Question;

public abstract class LinkedDataAnswerer {

	public abstract ArrayList<String> train(Question q, Answer a);
	public abstract ArrayList<String> test(Question q);
}
