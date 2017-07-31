;part 1
(defun all-length(X)
  (cond 
	((equal X nil) 0) ; if nil
	((listp (car X)) (+ (all-length(car X)) (all-length(cdr X)))) ; list
	((atom (car X)) (+ 1 (all-length(cdr X)))) ; just atom
  )
)

(defun range(X)
	(list (minl X) (maxl X)) ;return list of range
)
(defun minl(X)
	(cond
		((equal(cdr X) nil) (car X)) ;if last number
		(t (min (car X) (minl(cdr X)))) ;recursive
	)
)

(defun maxl(X)
	(cond 
		((equal(cdr X) nil) (car X)) ;if last number
		(t (max (car X) (maxl(cdr X)))) ;recursive
	)
)

(defun before(A B X)

	(cond
		((equal X nil) nil); if list is nil
		(t (before_help A B nil X)) ; runs before_help
		(t nil) ;
	)
)

(defun before_help(A B S L)
	(cond
		((equal L nil) nil)
		((and S (equal (car L) B)) (list(car L))); end
		(S (cons (car L) (before_help A B t (cdr L)))) ;seen
		((equal (car L) A) (cons (car L) (before_help A B t (cdr L))));found head
		(t (before_help A B S (cdr L))); go through
	)
)

(defun split-if(F L)
	(cond
		;((equal L nil) nil) ; list is nil
		(t (split-if_help F L '() '())) 
	)
)

(defun split-if_help(F L LF LT)
	(cond
		((equal L nil) (list LF LT)) ; list is nil
		((funcall F (car L)) (split-if_help F (cdr L) LF (append LT (list(car L))))) ; if function is true
		(t (split-if_help F (cdr L) (append LF (list(car L))) LT)); else function is false
	)
)


(defun group(X N)
	(cond
		((equal X nil) nil) ; list is nil
		(t (group_help X '() N)) ;	run group_help
	)
)

(defun group_help(L itrm n)
	(cond
		((equal L nil) (list itrm)) ; return the list
		((equal (length itrm) n) (cons itrm (group_help L '() n))) ; completed number
		(t (group_help (cdr L) (append itrm (list(car L))) n)) ; add to list
	)
)

(defun mostn(func L)
	(cond
		((equal L nil) nil) ; if nil
		(t (mostn_help func L '() 0)) ; calls func
	)
)

(defun mostn_help(f L itrm n)
	(cond
		((equal L nil) (list itrm n)) ; return what is found
		((> (funcall f (car L)) n) (mostn_help f (cdr L) (list(car L)) (funcall f (car L)))) ; save larger
		((equal (funcall f (car L)) n) (mostn_help f (cdr L)(append itrm (list(car L))) n)) ; add equal one
		(t (mostn_help f (cdr L) itrm n)) ; skip
	)
)
; part 2
(defun match(L1 L2)
	(cond
		((or (equal L1 nil) (equal L2 nil)) (equal L1 L2)); if one is nil
		((equal (car L1) '?) (match (cdr L1) (cdr L2))) ; if ?
		((and (equal (car L1) '!) (match (cdr L1) (cdr L2))) t) ; if ! and rest equal
		((equal (car L1) '!) (match L1 (cdr L2))) ; if ! and rest not equal
		(t (and (equal (car L1) (car L2)) (match (cdr L1) (cdr L2)))) ; regular matching
	)
)
; part 3
(defun match-var(l1 l2)
	(cond
		(t (match-var_help l1 l2 '())) ; if not empty
	)
)

(defun match-var_help(li1 li2 li3)
	(cond
		((or (equal li1 nil) (equal li2 nil)) (equal li1 li2)); if one is nil
		
		((and (listp (car li1)) (eq (caar li1) '?)) (set (cadar li1) (car li2)) ;if ?
		(match-var_help (cdr li1) (cdr li2) '())) ; if ?
			
		((and (listp (car li1)) (eq (caar li1) '!) (match-var_help (cdr li1) (cdr li2) li3)) ;if ! and rest true
		(set (cadar li1) '()) ; clear
		(set (cadar li1) (append li3 (list (car li2))));set
		t) ; if ! and take one
		
		((and (listp (car li1)) (eq (caar li1) '!)) ; if !
		(set (cadar li1) '()) ; clear	
		(set (cadar li1) (append (eval(cadar li1)) li3 (list (car li2)))) ;set
		(match-var_help li1 (cdr li2) (eval(cadar li1))) ) ; if ! and rest not equal
		
		(t (and (equal (car li1) (car li2))
		(match-var_help (cdr li1) (cdr li2) '()))) ; regular matching
	)
)
