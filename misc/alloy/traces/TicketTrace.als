module trace/TicketTrace
open Katze
open util/ordering[Project]

fact ticketTraces {
   no first.tickets
   all p : Project - last |
      let p' = next[p] |
      	 some t : Ticket | doTicket[p, p', t]
}
run {} for 5
