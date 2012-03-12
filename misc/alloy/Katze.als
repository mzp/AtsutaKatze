module katze

sig System {
   projects : set Project
}
sig Project {
  tickets : set Parent
}
sig Ticket {
  subs : set Sub
}

sig Parent extends Ticket {}
sig Sub    extends  Ticket {}

fact 孤立したプロジェクトやチケットはない {
   all p : Project | some s : System  | p in s.projects
   all t : Ticket  | some p : Project | t in p.tickets.*subs
}

// ============================================================
//  Project
// ============================================================
pred addProject(s1, s2 : System, p : Project) {
   not p in s1.projects
   no p.tickets
   s2.projects = s1.projects + p
}
pred delProject(s1, s2 : System, p : Project) {
   p in s1.projects
   s2.projects = s1.projects - p
}
pred doProject(s1, s2 : System, p : Project){
   addProject[s1, s2, p] or delProject[s1, s2, p]
}

// ============================================================
//  Ticket
// ============================================================
pred addTicket(p1, p2 : Project, t : Ticket){
   not t in p1.tickets
   no t.subs
   no t.~subs
   not t in t.subs
   p2.tickets = p1.tickets + t
}

pred delTicket(p1, p2 : Project, t : Ticket){
   t in p1.tickets
   p2.tickets = p1.tickets - t
}

pred doTicket(p1, p2 : Project, t : Ticket) {
   addTicket[p1, p2, t] or delTicket[p1, p2, t]
}

// ============================================================
// sub ticket
// ============================================================
pred addSubTicket(t1, t2 : Ticket, t : Ticket){
   not t in t1.subs
   no t.subs
   t2.subs = t1.subs + t
}

pred delSubTicket(t1, t2 : Ticket, t : Ticket){
   t in t1.subs
   t2.subs = t1.subs - t
}

pred doSubTicket(t1, t2 : Ticket, t : Sub){
   addSubTicket[t1, t2, t] or delSubTicket[t1, t2, t]
}
