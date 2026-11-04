import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"
import { Slot } from "radix-ui"

import { cn } from "@/lib/utils"

/**
 * Botões do arquivo-mestre: ".botao" + variação. Principal em Verde Bella com texto preto; ao passar o
 * mouse, Verde Musgo com texto branco. "faixa" é o botão preto da faixa verde; "barra", o branco da barra.
 */
const buttonVariants = cva(
  "botao disabled:pointer-events-none [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
  {
    variants: {
      variant: {
        default: "botao--principal",
        secondary: "botao--secundario",
        outline: "botao--secundario",
        destructive: "botao--perigo",
        ghost: "botao--fantasma",
        link: "botao--link",
        faixa: "botao--faixa",
        barra: "botao--barra",
      },
      size: {
        default: "",
        xs: "botao--compacto",
        sm: "botao--compacto",
        lg: "min-h-[52px]",
        icon: "botao--compacto w-9 px-0",
        "icon-xs": "botao--compacto w-8 px-0",
        "icon-sm": "botao--compacto w-9 px-0",
        "icon-lg": "w-[52px] px-0 min-h-[52px]",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

function Button({
  className,
  variant = "default",
  size = "default",
  asChild = false,
  ...props
}: React.ComponentProps<"button"> &
  VariantProps<typeof buttonVariants> & {
    asChild?: boolean
  }) {
  const Comp = asChild ? Slot.Root : "button"

  return (
    <Comp
      data-slot="button"
      data-variant={variant}
      data-size={size}
      className={cn(buttonVariants({ variant, size, className }))}
      {...props}
    />
  )
}

export { Button, buttonVariants }
