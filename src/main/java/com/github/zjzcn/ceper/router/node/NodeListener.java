package com.github.zjzcn.ceper.router.node;

import java.util.Set;

public interface NodeListener {

    void childhanged(Set<Node> nodes);
}
