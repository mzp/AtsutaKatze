module trace/SubTicketTrace
open Katze
open util/ordering[Parent]


fact subTicketTraces {
  no first.subs
  all t : Parent - last |
     let t' = next[t] |
      	some s : Sub | doSubTicket[t, t', s]
}
run {
  all t : Parent | t.subs in Sub

} for 5 but 3 Parent
