import * as React from "react"

import { cn } from "@/lib/utils"

/** Campo do arquivo-mestre (".campo input"): 44 px, borda cinza, raio 12, rótulo sempre visível fora dele. */
function Input({ className, type, ...props }: React.ComponentProps<"input">) {
  return (
    <input
      type={type}
      data-slot="input"
      className={cn("entrada", className)}
      {...props}
    />
  )
}

export { Input }
