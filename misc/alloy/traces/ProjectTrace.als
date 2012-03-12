module trace/ProjectTrace
open Katze
open util/ordering[System]

fact projectTrace {
   no first.projects
   all s : System - last |
      let s' = next[s] |
      	 some p : Project | doProject[s, s', p]
}
run {} for 5