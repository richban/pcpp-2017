-module(helloworld).
-export([start/0,sorter/0,merger/0,tester/0]).

% -- AUXILIARY FUNCTIONS ---------------------------------

n2s(N) -> lists:flatten(io_lib:format("~p",[N])).

% tostring to make sure there is no interleaving between printing elements
tostring([]) -> "\n";
tostring([H|T]) -> n2s(H) ++ tostring(T).

merge([],L) -> L;
merge(L,[]) -> L;
merge([H1|T1], [H2|T2]) ->
   case H1 < H2 of
      true -> [H1|merge(T1,[H2|T2])];
      false -> [H2|merge([H1|T1],T2)]
   end.

% -- ACTORS ----------------------------------------------

sorter() ->
   receive
      {sort, L, X} -> 
         % Sort list 'L' and send the result back to 'X':
         case length(L) > 1 of
            true -> 
               % The list 'L' is long, so cut it in two halves:
               M = 'spawn'(helloworld, merger, []),
               % instruct merger 'M' to send the result back to 'X':
               M ! {result, X},
               % split the list in two halves:
               {L1,L2} = lists:split(length(L) div 2, L),
               % spawn a sorter 'S1'...
               S1 = 'spawn'(helloworld, sorter, []),
               % ...instruct sorter 'S1' to sort 'L1' and send result to 'M1'
               S1 ! {sort, L1, M},
               % spawn a sorter 'S2'...
               S2 = 'spawn'(helloworld, sorter, []),
               % ...instruct sorter 'S2' to sort 'L2' and send result to 'M2'
               S2 ! {sort, L2, M};
            false -> 
               X ! {sorted, L}
         end
   end,
   sorter().
   
merger() ->
   receive
      {result, X} -> 
         receive
            {sorted, L1} ->
               receive 
                {sorted, L2} ->
                   L = merge(L1,L2),
                   % *** DEBUG: ***
                   io:fwrite("Merged: " ++ tostring(L)),
                   X ! {sorted, L}
              end
         end
   end.

tester() ->
   receive
      {init, Sorter} ->
         % sort 'List' and send the result back to me:
         L = [8,7,6,5,4,3,2,1],
         Sorter ! {sort, L, self()};
      {sorted, L} ->
         % print out the sorted list 'L':
         io:fwrite("Result: " ++ tostring(L))
   end,
   tester().

start() ->
   Tester = 'spawn'(helloworld, tester, []),
   Sorter = 'spawn'(helloworld, sorter, []),
   Tester ! {init, Sorter}.