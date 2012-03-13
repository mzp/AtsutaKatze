module trace/FullTrace
open Katze
open util/ordering[System]

fact fullTrace {
   no first.projects
   all s : System - last |
      let s' = next[s] |
      	 ((some p : Project | addProject[s, s', p]) or
   	  (some p : s.projects, p' : s'.projects, t : Ticket |
      	     s.projects - p = s'.projects - p' and addTicket[p, p', t]) or
   	  (some p : s.projects, p' : s'.projects, t : p.tickets , t' : p'.tickets, su : t.*subs, su' : t'.*subs,  c : Sub |
	     (s.projects - p = s'.projects - p' and
	      p.tickets  - t = p'.tickets  - t' and
          t.*subs - su.~*subs = t'.*subs - su'.~*subs and
	      addSubTicket[su, su', c])))
}


run {
 #Sub >= 3
} for 5 but 3 Parent, 3 Sub
