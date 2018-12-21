package com.intuit.fuzzymatcher.component;

import com.intuit.fuzzymatcher.domain.ElementType;
import com.intuit.fuzzymatcher.domain.Match;
import com.intuit.fuzzymatcher.domain.NGram;
import com.intuit.fuzzymatcher.domain.Token;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * Matches at Token level, this class uses the SimilarityMatchFunction to get a score at a Token level
 * This class also optimizes which tokens undergo match, by breaking it to NGram and figuring out the Search Groups
 */
@Component
public class TokenMatch {

    public Stream<Match<Token>> matchTokens(Stream<Token> input) {
        List<Token> tokenList = input.collect(Collectors.toList());
        initializeSearchGroups(tokenList);

        return tokenList.parallelStream()
                .filter(left -> BooleanUtils.isNotFalse(left.getElement().getDocument().isSource()))
                .flatMap(
                        left -> left.getSearchGroups()
                                .filter(right -> !left.getElement().getDocument().getKey().equals(right.getElement().getDocument().getKey()))
                                .map(right -> {
                                    double result = left.getElement().getSimilarityMatchFunction().apply(left, right);
                                    return new Match<Token>(left, right, result);
                                })
                                .filter(match -> match.getResult() >= match.getData().getElement().getThreshold())
                );
    }

    private void initializeSearchGroups(List<Token> input) {
        Stream<NGram> nGramStream = input.parallelStream().flatMap(token -> token.getNGrams());
        Map<ElementType, List<NGram>> elementTypeNGramMap = nGramStream
                .collect(Collectors.groupingBy(ngram -> ngram.getToken().getElement().getType()));

        elementTypeNGramMap.entrySet().parallelStream().forEach(entry -> {
            List<NGram> ngramsList = entry.getValue();
            Map<String, List<NGram>> stringNGramMap = ngramsList.parallelStream().collect(Collectors.groupingBy(NGram::getValue));

            stringNGramMap.entrySet().stream().forEach(stringListEntry -> {
                List<Token> groups = stringListEntry.getValue().parallelStream()
                        .map(NGram::getToken)
                        .distinct()
                        .collect(Collectors.toList());

                groups.parallelStream()
                        .filter(token -> BooleanUtils.isNotFalse(token.getElement().getDocument().isSource()))
                        .forEach(token -> token.setSearchGroups(Stream.concat(groups.stream(), token.getSearchGroups())));

            });
        });
    }
}
