fc_course(X):- course(X, _, N), N < 5, N > 2.

prereq_110(X):- course(X,Y,_), member(ecs110,Y).

ecs140a_students(X):- student(X,Y), member(ecs140a,Y).

instructor_names(X):- instructor(X,_), teach_john(X).

teach_john(X):- instructor(X,_0), student(john,Y), member(D,_0), member(D,Y), !.

students(X):- student(X,_1), jim_stu(X).
jim_stu(X):- student(X,_2), instructor(jim,T), member(D,_2), member(D,T), !.

allprereq(C,PS):- findall(CC,(course(CC,_,_), prereq(C,CC) ), PS).
prereq(C,P):- course(C,X,_), member(D,X), prereq(D,P); course(C,X,_), member(P,X).

all_length([],0).
all_length([H|T],Len):- atom(H), all_length(T,LenT), Len is LenT + 1.
all_length([H|T],Len):- all_length(H,LenL), all_length(T,LenT), Len is LenT + LenL, !.

equal_a_b(L):- equal_help(L,0,0).
equal_help([],A,A):- !.
equal_help([L|T],A,B):- 	L==a, A1 is A + 1, equal_help(T,A1,B);
							L==b, B1 is B + 1, equal_help(T,A,B1);
							L\=a, L\=b, equal_help(T,A,B).

							
swap_prefix_suffix(K,L,S):- swap(K,L,S).
swap(K,L,S):- prefix(P,L), suffix(SU,L), find(P,SU,X,L), K = X, append3(SU,K,P,S).
find(P,S,K,L):- suffix(_1,L), prefix(K,_1), append3(P,K,S,H), H == L, !.

append3(L1,L2,L3,L):- append(L1,LL,L), append(L2,L3,LL).

append([],Y,Y).
append([H|X],Y,[H|Z]):- append(X,Y,Z).

prefix(X,Z):- append(X,_3,Z).
suffix(Y,Z):- append(_4,Y,Z).


palin([]):- !.
palin(X):- reverse(X,RX), X == RX.

good([]):- !.
good([H|T]):- H==0, good(T), !;
			  H==1, good1(T,X), good(X).
good1([],_):- 1==2, !.
good1([H|T],X):- H==0, good10(T,X1), X = X1, !;
				 H==1, good1(T,X1), X = X1, !.
good10([],_):- 1==2, !.
good10([H|T],X):- H==0, X = T, !;
				  H==1, good1(T,X1), X = X1, !.
/*
state(F,W,G,C).
*/
opposite(A,B):- A \= B.
set(left,right):- !.
set(right,left):- !.
unsafe(state(X,Y,Y,C)):- opposite(X,Y).
unsafe(state(X,W,Y,Y)):- opposite(X,Y).
arc(take(none,X,Y), state(X,W,G,C), state(Y,W,G,C)):- set(X,Y).
arc(take(wolf,X,Y), state(X,X,G,C), state(Y,Y,G,C)):- set(X,Y).
arc(take(goat,X,Y), state(X,W,X,C), state(Y,W,Y,C)):- set(X,Y).
arc(take(cabbage,X,Y), state(X,W,G,X), state(Y,W,G,Y)):- set(X,Y).

solve:- go(state(left,left,left,left), state(right,right,right,right)).
go(X,Y):- go(X,Y,[]),!.
legal(X,[]).
legal(X,[H|T]):- \+ X = H, legal(X,T).
go(Y,Y,T):- !.
go(X,Y,T):- arc(Take,X,Z), legal(Z,T), \+ unsafe(Z), write(Take), nl, go(Z,Y,[Z|T]).
