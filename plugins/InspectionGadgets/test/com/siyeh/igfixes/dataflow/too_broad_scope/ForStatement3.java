package com.siyeh.igfixes.dataflow.too_broad_scope;

public class ForStatement3 {
  void m() {
    int <caret>i;
    for ((i) = 0; i < 10; i++) {
    }
  }
}