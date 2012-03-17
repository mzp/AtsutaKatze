module katze

sig System {
   project : RootProject
}

sig RootProject {
   projects : set Project
}

sig Project extends RootProject {}

fact no_orphan {
   all p : RootProject |
      some s : System |
         p in s.project.*projects
}

pred no_loop {
   all p : Project |
      not p in p.^projects
}

pred parent_only {
   all p : Project |
      lone p.~projects
}

pred show {
   no_loop
   parent_only
}

// run show for 5 but 1 System
open util/ordering[System]

pred addProject(s, s' : System, p : Project) {
   s'.project.^projects = s.project.^projects + p
}

pred project_trace {
   no first.project.projects
   all s : System - last |
      let s' = next[t] |
         some p : Project |
            addProject[s, s', p]
}

run project_trace for 5