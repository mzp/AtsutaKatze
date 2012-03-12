module assertion/SubTicketAssertion
open Katze
open traces/FullTrace

assert MutualRec {
   all disj s, s' : Sub |
      s' in s .~subs and
      s  in s'.~subs
}

check MutualRec for 5 but 3 Parent

assert SelfContain {
   all s : Sub | s in s.subs
}

check SelfContain for 5 but 3 Parent
