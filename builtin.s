	.text
	.align	2
	.globl	__Om_builtin_malloc
	.type	__Om_builtin_malloc, @function
__Om_builtin_malloc:
	tail	malloc
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
	mv		a1,a0
	lui		a0,%hi(.LC0)
	addi	a0,a0,%lo(.LC0)
	tail	printf
	.size	__Om_builtin_print, .-__Om_builtin_print
	.align	2
	.globl	__Om_builtin_println
	.type	__Om_builtin_println, @function
__Om_builtin_println:
	tail	puts
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
	mv		a1,a0
	lui		a0,%hi(.LC1)
	addi	a0,a0,%lo(.LC1)
	tail	printf
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
	mv		a1,a0
	lui		a0,%hi(.LC2)
	addi	a0,a0,%lo(.LC2)
	tail	printf
	.size	__Om_builtin_printlnInt, .-__Om_builtin_printlnInt
	.align	2
	.globl	__Om_builtin_getString
	.type	__Om_builtin_getString, @function
__Om_builtin_getString:
	li	a0,4096
	addi	sp,sp,-16
	addi	a0,a0,-1763
	sw	ra,12(sp)
	sw	s0,8(sp)
	call	malloc
	mv	s0,a0
	mv	a1,a0
	lui	a0,%hi(.LC0)
	addi	a0,a0,%lo(.LC0)
	call	scanf
	lw	ra,12(sp)
	mv	a0,s0
	lw	s0,8(sp)
	addi	sp,sp,16
	jr	ra
	.size	__Om_builtin_getString, .-__Om_builtin_getString
	.align	2
	.globl	__Om_builtin_getInt
	.type	__Om_builtin_getInt, @function
__Om_builtin_getInt:
	addi	sp,sp,-32
	lui	a0,%hi(.LC1)
	addi	a1,sp,12
	addi	a0,a0,%lo(.LC1)
	sw	ra,28(sp)
	call	scanf
	lw	ra,28(sp)
	lw	a0,12(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_getInt, .-__Om_builtin_getInt
	.align	2
	.globl	__Om_builtin_toString
	.type	__Om_builtin_toString, @function
__Om_builtin_toString:
	addi	sp,sp,-16
	sw	s1,4(sp)
	mv	s1,a0
	li	a0,23
	sw	ra,12(sp)
	sw	s0,8(sp)
	call	malloc
	lui	a1,%hi(.LC1)
	mv	a2,s1
	addi	a1,a1,%lo(.LC1)
	mv	s0,a0
	call	sprintf
	lw	ra,12(sp)
	mv	a0,s0
	lw	s0,8(sp)
	lw	s1,4(sp)
	addi	sp,sp,16
	jr	ra
	.size	__Om_builtin_toString, .-__Om_builtin_toString
	.align	2
	.globl	__Om_builtin_str_length
	.type	__Om_builtin_str_length, @function
__Om_builtin_str_length:
	tail strlen
	.size	__Om_builtin_str_length, .-__Om_builtin_str_length
	.align	2
	.globl	__Om_builtin_str_substring
	.type	__Om_builtin_str_substring, @function
__Om_builtin_str_substring:
	addi	sp,sp,-32
	sw	s0,24(sp)
	sub	s0,a2,a1
	sw	s3,12(sp)
	mv	s3,a0
	addi	a0,s0,1
	sw	ra,28(sp)
	sw	s1,20(sp)
	sw	s2,16(sp)
	mv	s2,a1
	call	malloc
	mv	s1,a0
	add	a1,s3,s2
	mv	a2,s0
	add	s0,s1,s0
	call	memcpy
	sb	zero,0(s0)
	lw	ra,28(sp)
	lw	s0,24(sp)
	lw	s2,16(sp)
	lw	s3,12(sp)
	mv	a0,s1
	lw	s1,20(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_str_substring, .-__Om_builtin_str_substring
	.align	2
	.globl	__Om_builtin_str_parseInt
	.type	__Om_builtin_str_parseInt, @function
__Om_builtin_str_parseInt:
	addi	sp,sp,-32
	lui	a1,%hi(.LC1)
	addi	a2,sp,12
	addi	a1,a1,%lo(.LC1)
	sw	ra,28(sp)
	call	sscanf
	lw	ra,28(sp)
	lw	a0,12(sp)
	addi	sp,sp,32
	jr	ra
	.size	__Om_builtin_str_parseInt, .-__Om_builtin_str_parseInt
	.align	2
	.globl	__Om_builtin_str_ord
	.type	__Om_builtin_str_ord, @function
__Om_builtin_str_ord:
	add	a0,a0,a1
	lbu	a0,0(a0)
	ret
	.size	__Om_builtin_str_ord, .-__Om_builtin_str_ord
	.align	2
	.globl	__Om_builtin_str_add
	.type	__Om_builtin_str_add, @function
__Om_builtin_str_add:
	addi	sp,sp,-16
	sw	ra,12(sp)
	sw	s0,8(sp)
	sw	s1,4(sp)
	sw	s2,0(sp)
	mv	s2,a0
	mv	s1,a1
	li	a0,4096
	addi	a0,a0,-1763
	call	malloc
	mv	s0,a0
	mv	a1,s2
	call	strcpy
	mv	a1,s1
	mv	a0,s0
	call	strcat
	mv	a0,s0
	lw	ra,12(sp)
	lw	s0,8(sp)
	lw	s1,4(sp)
	lw	s2,0(sp)
	addi	sp,sp,16
	jr	ra
	.size	__Om_builtin_str_add, .-__Om_builtin_str_add
	.align	2
	.globl	__Om_builtin_str_lt
	.type	__Om_builtin_str_lt, @function
__Om_builtin_str_lt:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	srli	a0,a0,31
	addi	sp,sp,16
	jr	ra
	.size	__Om_builtin_str_lt, .-__Om_builtin_str_lt
	.align	2
	.globl	__Om_builtin_str_gt
	.type	__Om_builtin_str_gt, @function
__Om_builtin_str_gt:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	sgt	a0,a0,zero
	addi	sp,sp,16
	jr	ra
	.size	__Om_builtin_str_gt, .-__Om_builtin_str_gt
	.align	2
	.globl	__Om_builtin_str_le
	.type	__Om_builtin_str_le, @function
__Om_builtin_str_le:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	slti	a0,a0,1
	addi	sp,sp,16
	jr	ra
	.size	__Om_builtin_str_le, .-__Om_builtin_str_le
	.align	2
	.globl	__Om_builtin_str_ge
	.type	__Om_builtin_str_ge, @function
__Om_builtin_str_ge:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	not	a0,a0
	srli	a0,a0,31
	addi	sp,sp,16
	jr	ra
	.size	__Om_builtin_str_ge, .-__Om_builtin_str_ge
	.align	2
	.globl	__Om_builtin_str_eq
	.type	__Om_builtin_str_eq, @function
__Om_builtin_str_eq:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	seqz	a0,a0
	addi	sp,sp,16
	jr	ra
	.size	__Om_builtin_str_eq, .-__Om_builtin_str_eq
	.align	2
	.globl	__Om_builtin_str_ne
	.type	__Om_builtin_str_ne, @function
__Om_builtin_str_ne:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	snez	a0,a0
	addi	sp,sp,16
	jr	ra
	.size	__Om_builtin_str_ne, .-__Om_builtin_str_ne