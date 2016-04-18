package org.jacoco.core.internal.analysis;

import org.jacoco.core.analysis.IHighlightNode;
import org.jacoco.report.internal.html.wrapper.NodeHighlightResults;

/**
 * Created by nkalonia1 on 3/17/16.
 */
public class MethodCoverageHighlight extends MethodCoverageImpl implements IHighlightNode {
    private NodeHighlightResults _nhr;

    public MethodCoverageHighlight(final String name, final String desc,
                                   final String signature) {
        super(name, desc, signature);
        _nhr = new NodeHighlightResults();
    }

    @Override
    public NodeHighlightResults getHighlightResults() {
        return _nhr;
    }
}