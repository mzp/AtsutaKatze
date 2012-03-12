module trace/FullTrace
open Katze
open util/ordering[System]

fact {
   all t : Ticket | no t.subs
}

fact fullTrace {
   no first.projects
   all s : System - last |
      let s' = next[s] |
      	 (some p : Project | doProject[s, s', p]) or
         (some p : s.projects, p' : s'.projects, t : Ticket |
      	    s.projects - p = s'.projects - p' and doTicket[p, p', t]) or
         (some t : s.projects.tickets.*subs, t' : s'.projects.tickets.*subs, su : Sub |
	       addSubTicket[t, t', su])
}


run {

} for 7 but 3 Parent
