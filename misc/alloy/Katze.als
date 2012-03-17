module katze
open util/ordering[System]

sig System {
   project : RootProject
}
sig AbstractProject {
   projects : set Project
}
sig RootProject extends AbstractProject{}
sig Project extends AbstractProject{}

fact no_orphan {
   all p : AbstractProject |
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


pred addProject(s, s' : System, p : Project) {
   no p.projects
   not p in s.project.^projects
   s'.project.^projects = s.project.^projects + p
}

pred project_trace {
   no first.project.projects
   all s : System - last |
      let s' = next[s] |
         some p : Project |
            addProject[s, s', p]
   some p : Project | #p.projects > 0
}

run project_trace for 5 but 10 AbstractProject
