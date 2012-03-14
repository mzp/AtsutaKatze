module Tree

sig Tree {
   children : set Node
}

sig Root extends Tree {
}

sig Node extends Tree {
}

fact no_orphan {
   all t : Tree |
      some r : Root |
         t in r.*children
}

pred parent_only(root : Root) {
   all t : root.*children |
      lone t.~children
}

pred invariant {
   all root : Root |  parent_only[root]
}

run invariant for 10 but 1 Root
