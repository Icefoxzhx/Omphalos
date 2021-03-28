	.text
	.align	2
	.globl	__Om_builtin_malloc
	.type	__Om_builtin_malloc, @function
__Om_builtin_malloc:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	lw	a5,-20(s0)
	mv	a0,a5
	call	malloc
	mv	a5,a0
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_malloc, .-__Om_builtin_malloc
	.section	.rodata
	.align	2
.LC0:
	.string	"%s"
	.text
	.align	2
	.globl	__Om_builtin_print
	.type	__Om_builtin_print, @function
__Om_builtin_print:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	lw	a1,-20(s0)
	lui	a5,%hi(.LC0)
	addi	a0,a5,%lo(.LC0)
	call	printf
	nop
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_print, .-__Om_builtin_print
	.align	2
	.globl	__Om_builtin_println
	.type	__Om_builtin_println, @function
__Om_builtin_println:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	lw	a0,-20(s0)
	call	puts
	nop
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_println, .-__Om_builtin_println
	.section	.rodata
	.align	2
.LC1:
	.string	"%d"
	.text
	.align	2
	.globl	__Om_builtin_printInt
	.type	__Om_builtin_printInt, @function
__Om_builtin_printInt:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	lw	a1,-20(s0)
	lui	a5,%hi(.LC1)
	addi	a0,a5,%lo(.LC1)
	call	printf
	nop
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_printInt, .-__Om_builtin_printInt
	.section	.rodata
	.align	2
.LC2:
	.string	"%d\n"
	.text
	.align	2
	.globl	__Om_builtin_printlnInt
	.type	__Om_builtin_printlnInt, @function
__Om_builtin_printlnInt:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	lw	a1,-20(s0)
	lui	a5,%hi(.LC2)
	addi	a0,a5,%lo(.LC2)
	call	printf
	nop
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_printlnInt, .-__Om_builtin_printlnInt
	.align	2
	.globl	__Om_builtin_getString
	.type	__Om_builtin_getString, @function
__Om_builtin_getString:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	li	a5,4096
	addi	a0,a5,-1763
	call	malloc
	mv	a5,a0
	sw	a5,-20(s0)
	lw	a1,-20(s0)
	lui	a5,%hi(.LC0)
	addi	a0,a5,%lo(.LC0)
	call	scanf
	lw	a5,-20(s0)
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_getString, .-__Om_builtin_getString
	.align	2
	.globl	__Om_builtin_getInt
	.type	__Om_builtin_getInt, @function
__Om_builtin_getInt:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	addi	a5,s0,-20
	mv	a1,a5
	lui	a5,%hi(.LC1)
	addi	a0,a5,%lo(.LC1)
	call	scanf
	lw	a5,-20(s0)
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_getInt, .-__Om_builtin_getInt
	.align	2
	.globl	__Om_builtin_toString
	.type	__Om_builtin_toString, @function
__Om_builtin_toString:
	addi	sp,sp,-48
	sw	ra,44(sp)
	sw	s0,40(sp)
	addi	s0,sp,48
	sw	a0,-36(s0)
	li	a0,23
	call	malloc
	mv	a5,a0
	sw	a5,-20(s0)
	lw	a2,-36(s0)
	lui	a5,%hi(.LC1)
	addi	a1,a5,%lo(.LC1)
	lw	a0,-20(s0)
	call	sprintf
	lw	a5,-20(s0)
	mv	a0,a5
	lw	ra,44(sp)
	lw	s0,40(sp)
	addi	sp,sp,48
	jr	ra
	.size	__Om_builtin_toString, .-__Om_builtin_toString
	.align	2
	.globl	__Om_builtin_str_length
	.type	__Om_builtin_str_length, @function
__Om_builtin_str_length:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	lw	a0,-20(s0)
	call	strlen
	mv	a5,a0
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_str_length, .-__Om_builtin_str_length
	.align	2
	.globl	__Om_builtin_str_substring
	.type	__Om_builtin_str_substring, @function
__Om_builtin_str_substring:
	addi	sp,sp,-48
	sw	ra,44(sp)
	sw	s0,40(sp)
	addi	s0,sp,48
	sw	a0,-36(s0)
	sw	a1,-40(s0)
	sw	a2,-44(s0)
	lw	a4,-44(s0)
	lw	a5,-40(s0)
	sub	a5,a4,a5
	addi	a5,a5,1
	mv	a0,a5
	call	malloc
	mv	a5,a0
	sw	a5,-20(s0)
	lw	a5,-40(s0)
	lw	a4,-36(s0)
	add	a3,a4,a5
	lw	a4,-44(s0)
	lw	a5,-40(s0)
	sub	a5,a4,a5
	mv	a2,a5
	mv	a1,a3
	lw	a0,-20(s0)
	call	memcpy
	lw	a4,-44(s0)
	lw	a5,-40(s0)
	sub	a5,a4,a5
	mv	a4,a5
	lw	a5,-20(s0)
	add	a5,a5,a4
	sb	zero,0(a5)
	lw	a5,-20(s0)
	mv	a0,a5
	lw	ra,44(sp)
	lw	s0,40(sp)
	addi	sp,sp,48
	jr	ra
	.size	__Om_builtin_str_substring, .-__Om_builtin_str_substring
	.align	2
	.globl	__Om_builtin_str_parseInt
	.type	__Om_builtin_str_parseInt, @function
__Om_builtin_str_parseInt:
	addi	sp,sp,-48
	sw	ra,44(sp)
	sw	s0,40(sp)
	addi	s0,sp,48
	sw	a0,-36(s0)
	addi	a5,s0,-20
	mv	a2,a5
	lui	a5,%hi(.LC1)
	addi	a1,a5,%lo(.LC1)
	lw	a0,-36(s0)
	call	sscanf
	lw	a5,-20(s0)
	mv	a0,a5
	lw	ra,44(sp)
	lw	s0,40(sp)
	addi	sp,sp,48
	jr	ra
	.size	__Om_builtin_str_parseInt, .-__Om_builtin_str_parseInt
	.align	2
	.globl	__Om_builtin_str_ord
	.type	__Om_builtin_str_ord, @function
__Om_builtin_str_ord:
	addi	sp,sp,-32
	sw	s0,28(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a5,-24(s0)
	lw	a4,-20(s0)
	add	a5,a4,a5
	lbu	a5,0(a5)
	mv	a0,a5
	lw	s0,28(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_str_ord, .-__Om_builtin_str_ord
	.align	2
	.globl	__Om_builtin_str_add
	.type	__Om_builtin_str_add, @function
__Om_builtin_str_add:
	addi	sp,sp,-48
	sw	ra,44(sp)
	sw	s0,40(sp)
	addi	s0,sp,48
	sw	a0,-36(s0)
	sw	a1,-40(s0)
	li	a5,4096
	addi	a0,a5,-1763
	call	malloc
	mv	a5,a0
	sw	a5,-20(s0)
	lw	a1,-36(s0)
	lw	a0,-20(s0)
	call	strcpy
	lw	a1,-40(s0)
	lw	a0,-20(s0)
	call	strcat
	lw	a5,-20(s0)
	mv	a0,a5
	lw	ra,44(sp)
	lw	s0,40(sp)
	addi	sp,sp,48
	jr	ra
	.size	__Om_builtin_str_add, .-__Om_builtin_str_add
	.align	2
	.globl	__Om_builtin_str_lt
	.type	__Om_builtin_str_lt, @function
__Om_builtin_str_lt:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a1,-24(s0)
	lw	a0,-20(s0)
	call	strcmp
	mv	a5,a0
	srli	a5,a5,31
	andi	a5,a5,0xff
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_str_lt, .-__Om_builtin_str_lt
	.align	2
	.globl	__Om_builtin_str_gt
	.type	__Om_builtin_str_gt, @function
__Om_builtin_str_gt:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a1,-24(s0)
	lw	a0,-20(s0)
	call	strcmp
	mv	a5,a0
	srli	a5,a5,31
	andi	a5,a5,0xff
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_str_gt, .-__Om_builtin_str_gt
	.align	2
	.globl	__Om_builtin_str_le
	.type	__Om_builtin_str_le, @function
__Om_builtin_str_le:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a1,-24(s0)
	lw	a0,-20(s0)
	call	strcmp
	mv	a5,a0
	slti	a5,a5,1
	andi	a5,a5,0xff
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_str_le, .-__Om_builtin_str_le
	.align	2
	.globl	__Om_builtin_str_ge
	.type	__Om_builtin_str_ge, @function
__Om_builtin_str_ge:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a1,-24(s0)
	lw	a0,-20(s0)
	call	strcmp
	mv	a5,a0
	not	a5,a5
	srli	a5,a5,31
	andi	a5,a5,0xff
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_str_ge, .-__Om_builtin_str_ge
	.align	2
	.globl	__Om_builtin_str_eq
	.type	__Om_builtin_str_eq, @function
__Om_builtin_str_eq:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a1,-24(s0)
	lw	a0,-20(s0)
	call	strcmp
	mv	a5,a0
	seqz	a5,a5
	andi	a5,a5,0xff
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_str_eq, .-__Om_builtin_str_eq
	.align	2
	.globl	__Om_builtin_str_ne
	.type	__Om_builtin_str_ne, @function
__Om_builtin_str_ne:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a1,-24(s0)
	lw	a0,-20(s0)
	call	strcmp
	mv	a5,a0
	snez	a5,a5
	andi	a5,a5,0xff
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_str_ne, .-__Om_builtin_str_ne